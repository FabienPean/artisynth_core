package artisynth.core.materials;

import maspack.matrix.Matrix3d;
import maspack.matrix.Matrix6d;
import maspack.matrix.SymmetricMatrix3d;
import maspack.properties.PropertyList;
import maspack.properties.PropertyUtils;
import maspack.util.DynamicArray;

public abstract class FemMaterial extends MaterialBase {

   static DynamicArray<Class<?>> mySubclasses = new DynamicArray<>(
      new Class<?>[] {
      LinearMaterial.class,
      StVenantKirchoffMaterial.class,
      MooneyRivlinMaterial.class,
      CubicHyperelastic.class,
      OgdenMaterial.class,
      FungMaterial.class,
      NeoHookeanMaterial.class,
      IncompNeoHookeanMaterial.class,
      IncompressibleMaterial.class,
      NullMaterial.class,
   });

   /**
    * Allow adding of classes (for use in control panels)
    * @param cls class to register
    */
   public static void registerSubclass(Class<? extends FemMaterial> cls) {
      if (!mySubclasses.contains(cls)) {
         mySubclasses.add(cls);
      }
   }

   public static Class<?>[] getSubClasses() {
      return mySubclasses.getArray();
   }

   ViscoelasticBehavior myViscoBehavior;

   protected void notifyHostOfPropertyChange () {
      notifyHostOfPropertyChange ("???");
   }

   public static PropertyList myProps =
      new PropertyList(FemMaterial.class, MaterialBase.class);

   static {
      myProps.add (
         "viscoBehavior", "visco elastic material behavior", null);
   }

   public PropertyList getAllPropertyInfo() {
      return myProps;
   }

   public ViscoelasticBehavior getViscoBehavior () {
      return myViscoBehavior;
   }

   /**
    * Allows setting of viscoelastic behaviour
    * @param veb visco-elastic behaviour
    */
   public void setViscoBehavior (ViscoelasticBehavior veb) {
      if (veb != null) {
         ViscoelasticBehavior newVeb = veb.clone();
         PropertyUtils.updateCompositeProperty (
            this, "viscoBehavior", null, newVeb);
         myViscoBehavior = newVeb;
         notifyHostOfPropertyChange ("viscoBehavior");
      }
      else if (myViscoBehavior != null) {
         PropertyUtils.updateCompositeProperty (
            this, "viscoBehavior", myViscoBehavior, null);
         myViscoBehavior = null;
         notifyHostOfPropertyChange ("viscoBehavior");
      }
   } 

   /**
    * Computes the tangent stiffness matrix
    * @param D tangent stiffness, populated
    * @param stress the current stress tensor
    * @param def deformation information, includes deformation gradient and pressure
    * @param Q coordinate frame specifying directions of anisotropy
    * @param baseMat underlying base material (if any)
    */
   public abstract void computeTangent (
      Matrix6d D, SymmetricMatrix3d stress, DeformedPoint def, 
      Matrix3d Q, FemMaterial baseMat);


   /**
    * Computes the strain tensor given the supplied deformation
    * @param sigma strain tensor, populated
    * @param def deformation information, includes deformation gradient and pressue
    * @param Q coordinate frame specifying directions of anisotropy
    * @param baseMat underlying base material (if any)
    */
   public abstract void computeStress (
      SymmetricMatrix3d sigma, DeformedPoint def, Matrix3d Q,
      FemMaterial baseMat);

   /**
    * Computes the current Cauchy stress and tangent stiffness matrix.
    * 
    * @param sigma returns the Cauchy stress
    * @param D optional; if non-{@code null}, returns the tangent matrix
    * @param def deformation information, including deformation gradient and 
    * pressure
    * @param Q coordinate frame specifying directions of anisotropy
    * @param excitation current excitation value
    */
   public void computeStressAndTangent (
      SymmetricMatrix3d sigma, Matrix6d D, DeformedPoint def, 
      Matrix3d Q, double excitation) {
      computeStress (sigma, def, Q, /*baseMat=*/null);
      if (D != null) {
         computeTangent (D, sigma, def, Q, /*baseMat=*/null);
      }
   }
   
   /**
    * Returns true if this material is defined for a deformation gradient
    * with a non-positive determinant.
    */
   public boolean isInvertible() {
      return false;
   }

   public boolean isIncompressible() {
      return false;
   }

   public boolean isViscoelastic() {
      return false;
   }
   
   /**
    * Linear stress/stiffness response to deformation, allows tangent
    * to be pre-computed and stored.
    * 
    * @return true if linear response
    */
   public boolean isLinear() {
      return false;
   }
   
   /**
    * Deformation is computed by first removing a rotation component 
    * (either explicit or computed from strain)
    * 
    * @return true if material is corotated
    */
   public boolean isCorotated() {
	  return false;
   }
   
   public boolean equals (FemMaterial mat) {
      return true;
   }

   public boolean equals (Object obj) {
      if (obj instanceof FemMaterial) {
         FemMaterial mat = (FemMaterial)obj;
         if (PropertyUtils.equalValues (myViscoBehavior, mat.myViscoBehavior)) {
            return equals (mat);
         }
      }
      return false;
   }

   public FemMaterial clone() {
      FemMaterial mat = (FemMaterial)super.clone();
      mat.setViscoBehavior (myViscoBehavior);
      return mat;
   }

   /**
    * Computes the right Cauchy-Green tensor from the deformation gradient.
    */
   public void computeRightCauchyGreen (
      SymmetricMatrix3d C, DeformedPoint def) {
      C.mulTransposeLeft (def.getF());
   }

   /**
    * Computes the left Cauchy-Green tensor from the deformation gradient.
    */
   public void computeLeftCauchyGreen (
      SymmetricMatrix3d B, DeformedPoint def) {
      B.mulTransposeRight (def.getF());
   }

   /**
    * Computes the right deviatoric Cauchy-Green tensor from the deformation
    * gradient.
    */
   public void computeDevRightCauchyGreen (
      SymmetricMatrix3d CD, DeformedPoint def) {
      CD.mulTransposeLeft (def.getF());
      CD.scale (Math.pow(def.getDetF(), -2.0/3.0));
   }

   /**
    * Computes the left deviatoric Cauchy-Green tensor from the deformation
    * gradient.
    */
   public void computeDevLeftCauchyGreen (
      SymmetricMatrix3d BD, DeformedPoint def) {
      BD.mulTransposeRight (def.getF());
      BD.scale (Math.pow(def.getDetF(), -2.0/3.0));
   }

   /**
    * Computes the second Piola-Kirchoff stress tensor from the Cauchy stress,
    * according to the formula
    *
    * S = J F^{-1} sigma F^{-T}
    */
   public static void cauchyToSecondPKStress (
      SymmetricMatrix3d S, SymmetricMatrix3d sigma, DeformedPoint def) {

      Matrix3d Finv = new Matrix3d();
      Finv.fastInvert (def.getF());
      S.set (sigma);
      S.mulLeftAndTransposeRight (Finv);
      S.scale (def.getDetF());
   }

   /**
    * Computes the Cauchy stress from the second Piola-Kirchoff stress tensor,
    * according to the formula
    *
    * sigma = 1/J F sigma F^T
    */
   public static void secondPKToCauchyStress (
      SymmetricMatrix3d sigma, SymmetricMatrix3d S, DeformedPoint def) {

      sigma.set (S);
      sigma.mulLeftAndTransposeRight (def.getF());
      sigma.scale (1/def.getDetF());
   }

   public boolean hasState() {
      return myViscoBehavior != null && !isLinear();
   }

   public MaterialStateObject createStateObject() {
      return null;
   }

}
