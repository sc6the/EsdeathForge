package net.labymod.user.cosmetic.geometry.render;

public class GeometryVector3 {
   public final double xCoord;
   public final double yCoord;
   public final double zCoord;

   public GeometryVector3(double x, double y, double z) {
      if (x == -0.0) {
         x = 0.0;
      }

      if (y == -0.0) {
         y = 0.0;
      }

      if (z == -0.0) {
         z = 0.0;
      }

      this.xCoord = x;
      this.yCoord = y;
      this.zCoord = z;
   }

   public GeometryVector3 subtractReverse(GeometryVector3 vec) {
      return new GeometryVector3(vec.xCoord - this.xCoord, vec.yCoord - this.yCoord, vec.zCoord - this.zCoord);
   }

   public GeometryVector3 normalize() {
      double d0 = Math.sqrt(this.xCoord * this.xCoord + this.yCoord * this.yCoord + this.zCoord * this.zCoord);
      return d0 < 1.0E-4 ? new GeometryVector3(0.0, 0.0, 0.0) : new GeometryVector3(this.xCoord / d0, this.yCoord / d0, this.zCoord / d0);
   }

   public double dotProduct(GeometryVector3 vec) {
      return this.xCoord * vec.xCoord + this.yCoord * vec.yCoord + this.zCoord * vec.zCoord;
   }

   public GeometryVector3 crossProduct(GeometryVector3 vec) {
      return new GeometryVector3(
         this.yCoord * vec.zCoord - this.zCoord * vec.yCoord,
         this.zCoord * vec.xCoord - this.xCoord * vec.zCoord,
         this.xCoord * vec.yCoord - this.yCoord * vec.xCoord
      );
   }

   public GeometryVector3 subtract(GeometryVector3 vec) {
      return this.subtract(vec.xCoord, vec.yCoord, vec.zCoord);
   }

   public GeometryVector3 subtract(double x, double y, double z) {
      return this.addVector(-x, -y, -z);
   }

   public GeometryVector3 add(GeometryVector3 vec) {
      return this.addVector(vec.xCoord, vec.yCoord, vec.zCoord);
   }

   public GeometryVector3 addVector(double x, double y, double z) {
      return new GeometryVector3(this.xCoord + x, this.yCoord + y, this.zCoord + z);
   }

   public double distanceTo(GeometryVector3 vec) {
      double d0 = vec.xCoord - this.xCoord;
      double d1 = vec.yCoord - this.yCoord;
      double d2 = vec.zCoord - this.zCoord;
      return Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
   }

   public double squareDistanceTo(GeometryVector3 vec) {
      double d0 = vec.xCoord - this.xCoord;
      double d1 = vec.yCoord - this.yCoord;
      double d2 = vec.zCoord - this.zCoord;
      return d0 * d0 + d1 * d1 + d2 * d2;
   }

   public double lengthVector() {
      return Math.sqrt(this.xCoord * this.xCoord + this.yCoord * this.yCoord + this.zCoord * this.zCoord);
   }

   public GeometryVector3 getIntermediateWithXValue(GeometryVector3 vec, double x) {
      double d0 = vec.xCoord - this.xCoord;
      double d1 = vec.yCoord - this.yCoord;
      double d2 = vec.zCoord - this.zCoord;
      if (d0 * d0 < 1.0E-7F) {
         return null;
      } else {
         double d3 = (x - this.xCoord) / d0;
         return d3 >= 0.0 && d3 <= 1.0 ? new GeometryVector3(this.xCoord + d0 * d3, this.yCoord + d1 * d3, this.zCoord + d2 * d3) : null;
      }
   }

   public GeometryVector3 getIntermediateWithYValue(GeometryVector3 vec, double y) {
      double d0 = vec.xCoord - this.xCoord;
      double d1 = vec.yCoord - this.yCoord;
      double d2 = vec.zCoord - this.zCoord;
      if (d1 * d1 < 1.0E-7F) {
         return null;
      } else {
         double d3 = (y - this.yCoord) / d1;
         return d3 >= 0.0 && d3 <= 1.0 ? new GeometryVector3(this.xCoord + d0 * d3, this.yCoord + d1 * d3, this.zCoord + d2 * d3) : null;
      }
   }

   public GeometryVector3 getIntermediateWithZValue(GeometryVector3 vec, double z) {
      double d0 = vec.xCoord - this.xCoord;
      double d1 = vec.yCoord - this.yCoord;
      double d2 = vec.zCoord - this.zCoord;
      if (d2 * d2 < 1.0E-7F) {
         return null;
      } else {
         double d3 = (z - this.zCoord) / d2;
         return d3 >= 0.0 && d3 <= 1.0 ? new GeometryVector3(this.xCoord + d0 * d3, this.yCoord + d1 * d3, this.zCoord + d2 * d3) : null;
      }
   }

   @Override
   public String toString() {
      return "(" + this.xCoord + ", " + this.yCoord + ", " + this.zCoord + ")";
   }

   public GeometryVector3 rotatePitch(float pitch) {
      float f = (float)Math.cos((double)pitch);
      float f1 = (float)Math.sin((double)pitch);
      double d0 = this.xCoord;
      double d1 = this.yCoord * (double)f + this.zCoord * (double)f1;
      double d2 = this.zCoord * (double)f - this.yCoord * (double)f1;
      return new GeometryVector3(d0, d1, d2);
   }

   public GeometryVector3 rotateYaw(float yaw) {
      float f = (float)Math.cos((double)yaw);
      float f1 = (float)Math.sin((double)yaw);
      double d0 = this.xCoord * (double)f + this.zCoord * (double)f1;
      double d1 = this.yCoord;
      double d2 = this.zCoord * (double)f - this.xCoord * (double)f1;
      return new GeometryVector3(d0, d1, d2);
   }
}
