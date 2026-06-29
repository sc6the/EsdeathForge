package me.proxycracked.universalaccountmanager.auth;

public class Account {
  public static final String TYPE_MS = "ms";
  public static final String TYPE_TOKEN = "token";
  public static final String TYPE_COOKIE = "cookie";
  public static final String TYPE_LAUNCHER = "launcher";
  // A refresh-token account: stores a long-lived MS refresh token (legacy
  // launcher client) and re-derives a fresh access token every list refresh.
  // Behaves like a normal token account once an access token is present.
  public static final String TYPE_REFRESH = "refresh";

  private String type;
  private String refreshToken;
  private String accessToken;
  private String username;
  private String uuid;
  private boolean pinned;
  // Path to the stored cookie export (Netscape cookies.txt) for cookie
  // accounts. Lets us re-derive a fresh access token from the long-lived
  // login.live.com cookies on every list refresh instead of letting the
  // short-lived MC access token expire. Empty for non-cookie accounts.
  private String cookieFile;
  // Transient — runtime availability check result. null = not yet checked.
  private transient Boolean available;

  public Account(String type, String refreshToken, String accessToken,
                 String username, String uuid) {
    this.type = type == null ? TYPE_MS : type;
    this.refreshToken = refreshToken == null ? "" : refreshToken;
    this.accessToken = accessToken == null ? "" : accessToken;
    this.username = username == null ? "" : username;
    this.uuid = uuid == null ? "" : uuid;
  }

  public Account(String refreshToken, String accessToken, String username) {
    this(TYPE_MS, refreshToken, accessToken, username, "");
  }

  public String getType() { return type; }
  public String getRefreshToken() { return refreshToken; }
  public String getAccessToken() { return accessToken; }
  public String getUsername() { return username; }
  public String getUuid() { return uuid; }

  public void setType(String type) { this.type = type; }
  public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
  public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
  public void setUsername(String username) { this.username = username; }
  public void setUuid(String uuid) { this.uuid = uuid; }

  // Refresh accounts log in via their derived access token, so they count as
  // token accounts for the login/validation flow.
  public boolean isToken() { return TYPE_TOKEN.equals(type) || TYPE_LAUNCHER.equals(type) || TYPE_REFRESH.equals(type); }
  public boolean isCookie() { return TYPE_COOKIE.equals(type); }
  public boolean isLauncher() { return TYPE_LAUNCHER.equals(type); }
  public boolean isRefresh() { return TYPE_REFRESH.equals(type); }
  public boolean isMs() { return TYPE_MS.equals(type); }

  public Boolean getAvailable() { return available; }
  public void setAvailable(Boolean available) { this.available = available; }

  public boolean isPinned() { return pinned; }
  public void setPinned(boolean pinned) { this.pinned = pinned; }

  public String getCookieFile() { return cookieFile == null ? "" : cookieFile; }
  public void setCookieFile(String cookieFile) { this.cookieFile = cookieFile; }
}
