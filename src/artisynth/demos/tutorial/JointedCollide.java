package artisynth.demos.tutorial;

import maspack.geometry.*;
import maspack.spatialmotion.*;
import maspack.matrix.*;
import maspack.render.*;
import maspack.util.*;
import artisynth.core.mechmodels.*;
import artisynth.core.modelbase.*;
import artisynth.core.probes.WayPoint;
import artisynth.core.driver.*;
import artisynth.core.util.*;
import artisynth.core.workspace.RootModel;
import artisynth.core.gui.*;
import maspack.render.*;

import java.awt.Color;
import java.io.*;

import javax.swing.*;

public class JointedCollide extends RigidBodyJoint {

   public void build (String[] args) {

      super.build (args);

      link1.setDynamic (true);

      RigidBody base = RigidBody.createBox ("base", 25, 25, 2, 0.2);
      base.setPose (new RigidTransform3d (5, 0, 0, 0, 1, 0, -Math.PI/8));
      base.setDynamic (false);

      mech.addRigidBody (base);
      mech.setDefaultCollisionBehavior (true, 0.20);
      mech.setCollisionBehavior (link1, link2, false);
   }

}
