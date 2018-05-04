package artisynth.core.materials;

import maspack.matrix.*;

/**
 * Base class for deformed point structure. Alows quantities
 */
public class DeformedPointBase implements DeformedPoint {

   protected Matrix3d myF;
   protected double myDetF;
   protected double myP;
   protected RotationMatrix3d myR;

   protected Point3d myRestPos;
   protected Point3d mySpatialPos;

   protected int[] myNodeNumbers;
   protected double[] myNodeWeights;
   protected int myElemNum;
   protected int myPointIdx = -1;

   public DeformedPointBase() {
      myF = new Matrix3d();
      myDetF = 0;
      myR = null;
      myP = 0;

      myPointIdx = -1;
      myNodeNumbers = null;
      myNodeWeights = null;
      myElemNum = -1;  

      myRestPos = new Point3d();
      mySpatialPos = new Point3d();
   }
   
   public Matrix3d getF() {
      return myF;
   }

   public double getDetF() {
      return myDetF;
   }
   
   public void setF (Matrix3d F) {
      myF.set (F);
      myDetF = F.determinant();
   }

   public double getAveragePressure() {
      return myP;
   }

   public void setAveragePressure (double p) {
      myP = p;
   }
   
   public void setR(Matrix3dBase R) {
      if (R == null) {
         myR = null;
         return;
      }
      if (myR == null) {
         myR = new RotationMatrix3d();
      }
      myR.set(R);
   }   

   public RotationMatrix3d getR() {
      return myR;
   }
   
   public Point3d getSpatialPos() {
      return mySpatialPos;
   }

   public Point3d getRestPos() {
      return myRestPos;
   }

   public int getElementNumber() {
      return myElemNum;
   }

   public int getPointIndex() {
      return myPointIdx;
   }

   public int[] getNodeNumbers() {
      return myNodeNumbers;
   }

   public double[] getNodeWeights() {
      return myNodeWeights;
   }
   
}
