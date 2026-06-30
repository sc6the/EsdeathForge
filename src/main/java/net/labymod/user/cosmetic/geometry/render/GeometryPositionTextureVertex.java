package net.labymod.user.cosmetic.geometry.render;

public class GeometryPositionTextureVertex {
   public GeometryVector3 vector3D;
   public float texturePositionX;
   public float texturePositionY;

   public GeometryPositionTextureVertex(float p_i1158_1_, float p_i1158_2_, float p_i1158_3_, float p_i1158_4_, float p_i1158_5_) {
      this(new GeometryVector3((double)p_i1158_1_, (double)p_i1158_2_, (double)p_i1158_3_), p_i1158_4_, p_i1158_5_);
   }

   public GeometryPositionTextureVertex setTexturePosition(float p_78240_1_, float p_78240_2_) {
      return new GeometryPositionTextureVertex(this, p_78240_1_, p_78240_2_);
   }

   public GeometryPositionTextureVertex(GeometryPositionTextureVertex textureVertex, float texturePositionXIn, float texturePositionYIn) {
      this.vector3D = textureVertex.vector3D;
      this.texturePositionX = texturePositionXIn;
      this.texturePositionY = texturePositionYIn;
   }

   public GeometryPositionTextureVertex(GeometryVector3 vector3DIn, float texturePositionXIn, float texturePositionYIn) {
      this.vector3D = vector3DIn;
      this.texturePositionX = texturePositionXIn;
      this.texturePositionY = texturePositionYIn;
   }
}
