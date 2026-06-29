package me.proxycracked.universalaccountmanager.auth;

import me.proxycracked.universalaccountmanager.UniversalAccountManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

// Async account-availability checks. Hits /minecraft/profile with the stored
// access token; 200 → available, 401 → unavailable. MS access tokens expire
// hourly so we don't currently flag them as unavailable on a 401 (the user can
// recover via the normal Login flow which refreshes the token).
public final class AccountValidator {
  private AccountValidator() {}

  private static final ExecutorService EXEC = Executors.newFixedThreadPool(3, new ThreadFactory() {
    private final AtomicInteger n = new AtomicInteger(0);
    @Override public Thread newThread(Runnable r) {
      Thread t = new Thread(r, "UniversalAccountManager-Validate-" + n.incrementAndGet());
      t.setDaemon(true);
      return t;
    }
  });

  public static void validateAll() {
    for (Account acc : UniversalAccountManager.accounts) validate(acc);
  }

  public static void validate(final Account account) {
    if (account == null || account.getAccessToken() == null || account.getAccessToken().isEmpty()) {
      account.setAvailable(Boolean.FALSE);
      return;
    }
    EXEC.submit(() -> {
      try {
        boolean ok = TokenAuth.validate(account.getAccessToken());
        if (ok) {
          account.setAvailable(Boolean.TRUE);
        } else if (account.isRefresh()
            || (account.isCookie() && !account.getCookieFile().isEmpty())) {
          // Self-refreshing accounts re-derive a fresh token on every list
          // refresh, so a stale access token here isn't fatal — leave the flag
          // TRUE to avoid greying the account out before the refresh lands.
          account.setAvailable(Boolean.TRUE);
        } else if (account.isToken() || account.isCookie()) {
          account.setAvailable(Boolean.FALSE);
        } else {
          // MS access tokens expire hourly; a single 401 doesn't mean the
          // account is dead. Leave the flag as TRUE so the user can refresh
          // via the Login button instead of seeing it greyed out.
          account.setAvailable(Boolean.TRUE);
        }
      } catch (Exception e) {
        // Network failure — don't flip a previously-known flag, just leave it.
        if (account.getAvailable() == null) account.setAvailable(Boolean.TRUE);
      }
    });
  }
}
