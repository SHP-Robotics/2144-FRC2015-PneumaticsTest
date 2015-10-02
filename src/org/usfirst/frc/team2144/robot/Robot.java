package org.usfirst.frc.team2144.robot;

import com.ni.vision.NIVision;
import com.ni.vision.NIVision.DrawMode;
import com.ni.vision.NIVision.Image;
import com.ni.vision.NIVision.ShapeMode;

import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.command.Scheduler;
import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.vision.AxisCamera;
import edu.wpi.first.wpilibj.CANTalon;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.I2C;
import edu.wpi.first.wpilibj.Relay;
import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Gyro;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.PowerDistributionPanel;
import edu.wpi.first.wpilibj.Compressor;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class Robot extends IterativeRobot {
	RobotDrive myRobot;
	Joystick stick;
	Joystick stick2;
	DigitalInput winchtopL; // 0: TopLeft; 2 orange/1 green, 1: BottomLeft;
							// 2grn/1blu, 2: TOPRight; ?, 3: BottomRight;
	DigitalInput winchbottomL;
	DigitalInput winchtopR;
	DigitalInput winchbottomR;
	Relay spike;
	Solenoid out;
	Solenoid in;
	// Gyro gyro;
	// I2C i2c;
	Talon winch;
	PowerDistributionPanel pdp;
	Compressor pneumatics;
	Servo cameraX;
	Servo cameraY;
	//Image image;
	//AxisCamera camera;
	// CameraServer cameraServer = new CameraServer.getInstance();
	// Encoder test;
	int autoLoopCounter;
	int cameraXPos = 153;
	int cameraYPos = 67;
	boolean camLEDs = false;
	boolean gotBin = false;
	boolean stopOverride = false;
	boolean eDrop = false;
	double speedMultiplier = 1;
	double mecanumMultiplier = 1;
	double winchMultiplier = 0.8;
	int eDropTime = 40;

	int pneumaticsState;
	SendableChooser pneumaticsChooser;

	/**
	 * This function is run when the robot is first started up and should be
	 * used for any initialization code.
	 */
	public void robotInit() {

		pneumaticsChooser = new SendableChooser();
		pneumaticsChooser.addDefault("Enable Pneumatics", 0);
		pneumaticsChooser.addObject("Disable Pneumatics", 1);
		SmartDashboard.putData("Pneumatics", pneumaticsChooser);

		//image = NIVision.imaqCreateImage(NIVision.ImageType.IMAGE_RGB, 0);

		// open the camera at the IP address assigned. This is the IP address
		// that the camera
		// can be accessed through the web interface.
		//camera = new AxisCamera("axis-camera2144.local");

		//CameraServer.getInstance().setQuality(50);

		myRobot = new RobotDrive(0, 1, 2, 3);// 2:Green, 3:Pink, 0:Blue,
												// 1:Orange
		stick = new Joystick(0);
		stick2 = new Joystick(1);
		winchtopL = new DigitalInput(0);
		winchtopR = new DigitalInput(2);
		winchbottomL = new DigitalInput(1);
		winchbottomR = new DigitalInput(3);
		pdp = new PowerDistributionPanel();
		pneumatics = new Compressor();
		spike = new Relay(0);
		out = new Solenoid(0);
		in = new Solenoid(1);
		winch = new Talon(4);
		cameraX = new Servo(9);
		cameraY = new Servo(6);
		// gyro = new Gyro(0);
		myRobot.setInvertedMotor(RobotDrive.MotorType.kRearLeft, true);
		myRobot.setInvertedMotor(RobotDrive.MotorType.kFrontLeft, true);
		// i2c = new I2C(I2C.Port.kOnboard, 168);
		pdp.clearStickyFaults();
		pneumatics.clearAllPCMStickyFaults();

		// winch.changeControlMode(CANTalon.ControlMode.PercentVbus);
		// winch.enableControl();
		// CameraServer.getInstance().startAutomaticCapture("cam0");
		stopOverride = false;

	}

	/**
	 * This function is run once each time the robot enters autonomous mode
	 */
	public void autonomousInit() {
		SmartDashboard.putData("Pneumatics", pneumaticsChooser);

		autoLoopCounter = 0;
		out.set(true);
		in.set(false);
		myRobot.setInvertedMotor(RobotDrive.MotorType.kRearLeft, false);
		myRobot.setInvertedMotor(RobotDrive.MotorType.kFrontLeft, false);
	}

	/**
	 * This function is called periodically during autonomous
	 */
	public void autonomousPeriodic() {
		pneumaticsState = (Integer) pneumaticsChooser.getSelected();
		if (pneumaticsState == 0) {
			pneumatics.start();
			SmartDashboard.putNumber("PneumaticsState", 1);
		} else {
			pneumatics.stop();
			SmartDashboard.putNumber("PneumaticsState", 0);
		}
		Scheduler.getInstance().run();

		

		/*
		 * if(autoLoopCounter <100){ myRobot.arcadeDrive(0, 0.5); } else{
		 * myRobot.arcadeDrive(0, -0.5);//turns right } autoLoopCounter++;
		 */
		if (!winchbottomL.get() || !winchbottomR.get() || gotBin) {
			gotBin = true;
			/*if (autoLoopCounter < 120) {
				winch.set(-0.1);
				myRobot.arcadeDrive(0.5, -0.1);
			} else */if (autoLoopCounter > 120 && autoLoopCounter < 130) {
				// myRobot.arcadeDrive(0,-0.4);
				out.set(false);
				in.set(true);
			} else if (autoLoopCounter > 150 && autoLoopCounter < 210) {
				winch.set(-0.5);
			}
			/*
			 * else if(autoLoopCounter>320 && autoLoopCounter < 350){
			 * winch.set(-0.1); myRobot.arcadeDrive(0.6,0); } else
			 * if(autoLoopCounter>350 && autoLoopCounter<430 &&
			 * winchbottomL.get() && winchbottomR.get()){ winch.set(0.2);
			 * out.set(true); in.set(false); } else if(autoLoopCounter>430 &&
			 * autoLoopCounter<440){ in.set(true); out.set(false); } else
			 * if(autoLoopCounter>450 && autoLoopCounter<530){ winch.set(-0.5);
			 * myRobot.arcadeDrive(0, -0.2); } else if(autoLoopCounter>530 &&
			 * autoLoopCounter < 1060){ winch.set(-0.1);
			 * myRobot.arcadeDrive(0.6,0); }//add end comment here
			 */
			else if (autoLoopCounter > 210 && autoLoopCounter < 500) {
				winch.set(-0.1);
				myRobot.arcadeDrive(0.6, -0.23);
			} else if (autoLoopCounter > 500 && autoLoopCounter < 580
					&& winchbottomL.get() && winchbottomR.get()) {
				winch.set(0.2);
				myRobot.arcadeDrive(0, 0);
				out.set(true);
				in.set(false);
			} else if (autoLoopCounter > 580 && autoLoopCounter < 620) {
				winch.set(-0.1);
				myRobot.arcadeDrive(-0.6, -0.23);
			}

			autoLoopCounter++;
		} else {
			winch.set(0.3);

		}
		// autoLoopCounter++;
	}

	/**
	 * This function is called once each time the robot enters tele-operated
	 * mode
	 */
	public void teleopInit() {
		SmartDashboard.putData("Pneumatics", pneumaticsChooser);

		myRobot.setInvertedMotor(RobotDrive.MotorType.kRearLeft, true);
		myRobot.setInvertedMotor(RobotDrive.MotorType.kFrontLeft, true);
		out.set(true);
		in.set(false);
		// CameraServer.getInstance().getInstance().getInstance().getInstance().startAutomaticCapture();

	}

	// Hi Ender and Giorgio approves!!!

	/**
	 * This function is called periodically during operator control
	 */
	public void teleopPeriodic() {
		pneumaticsState = (Integer) pneumaticsChooser.getSelected();
		if (pneumaticsState == 0) {
			pneumatics.start();
			SmartDashboard.putNumber("PneumaticsState", 1);
		} else {
			pneumatics.stop();
			SmartDashboard.putNumber("PneumaticsState", 0);
		}
		Scheduler.getInstance().run();
		//camera.getImage(image);
		//NIVision.imaqFlip(image, image, NIVision.FlipAxis.HORIZONTAL_AXIS);
		//NIVision.imaqFlip(image, image, NIVision.FlipAxis.VERTICAL_AXIS);

		//CameraServer.getInstance().setImage(image);
		/*
		 * if(stick.getRawButton(4) && stick.getRawButton(1)){//drive code
		 * myRobot.mecanumDrive_Polar(0.3, 90, -1*stick.getX()); } else
		 * if(stick.getRawButton(5) && stick.getRawButton(1)){
		 * myRobot.mecanumDrive_Polar(0.3, 270, -1*stick.getX()); } else
		 * if(stick.getRawButton(4)){//drive code myRobot.mecanumDrive_Polar(1,
		 * 90, -1*stick.getX()); } else if(stick.getRawButton(5)){
		 * myRobot.mecanumDrive_Polar(1, 270, -1*stick.getX()); }
		 * 
		 * else{ if(stick.getRawButton(1)){
		 * myRobot.arcadeDrive(stick.getX()*-0.5, stick.getY()*-0.5); } else{
		 * myRobot.arcadeDrive(stick.getX()*-1, stick.getY()*-1); }
		 * 
		 * }
		 */

		if (stick.getRawButton(1)) {
			speedMultiplier = 0.5;
			mecanumMultiplier = 0.4;
		} else {
			if (speedMultiplier < 1) {
				speedMultiplier += 0.05;
			}
			if (mecanumMultiplier < 1) {
				mecanumMultiplier += 0.1;
			}
		}

		if (stick.getRawButton(4) && stick.getRawButton(1)) {// drive code
			myRobot.mecanumDrive_Polar(0.3, 90, -1 * stick.getX());
		} else if (stick.getRawButton(5) && stick.getRawButton(1)) {
			myRobot.mecanumDrive_Polar(0.3, 270, -1 * stick.getX());
		} else if (stick.getRawButton(4)) {// drive code
			myRobot.mecanumDrive_Polar(mecanumMultiplier, 90, -1 * stick.getX());
		} else if (stick.getRawButton(5)) {
			myRobot.mecanumDrive_Polar(mecanumMultiplier, 270,
					-1 * stick.getX());
		}

		else {
			if (stick.getRawButton(1)) {
				myRobot.arcadeDrive(stick.getX() * -0.5, stick.getY() * -0.5);
			} else {
				myRobot.arcadeDrive(stick.getX() * -speedMultiplier,
						stick.getY() * -speedMultiplier);
			}
		}

		if (stick2.getRawButton(4)) {// pneumatics
			out.set(false);
			in.set(true);
		} else if (stick2.getRawButton(3)) {
			out.set(true);
			in.set(false);
		}

		if (stick2.getRawButton(7)) {// stop end switch override
			stopOverride = true;
		}

		if (stick2.getRawButton(11)) {// EMERGENCY DROP
			eDrop = true;
		}
		
		
		if (!eDrop) {
			if (!winchtopL.get() || !winchtopR.get() && !stopOverride) {// if touch
																		// sensor at
																		// top is
																		// pressed,
																		// then...
				if (stick2.getY() > 0) {// if trying to go up, set motor speed to 0
										// (Not moving)
					winch.set(-0.1);
				} else {
					winch.set(stick2.getY() * -winchMultiplier);// otherwise go down
				}
			} else if (!winchbottomL.get() || !winchbottomR.get() && !stopOverride) {// if
																						// touch
																						// sensor
																						// at
																						// bottom
																						// is
																						// pressed,
																						// then...
				if (stick2.getY() < 0) {// if trying to go down, set motor speed to
										// 0 (Not moving)
					winch.set(-0.1);
				} else {
					winch.set(stick2.getY() * -winchMultiplier);// otherwise go up
				}
			}
			if (!winchtopL.get() || !winchtopR.get() && !stopOverride
					&& stick2.getRawButton(1)) {// if touch sensor at top is
												// pressed, then...
				if (stick2.getY() > 0) {// if trying to go up, set motor speed to 0
										// (Not moving)
					winch.set(-0.1);
				} else {
					winch.set(stick2.getY() * -0.5);// otherwise go down
				}
			} else if (!winchbottomL.get() || !winchbottomR.get() && !stopOverride
					&& stick2.getRawButton(1)) {// if touch sensor at bottom is
												// pressed, then...
				if (stick2.getY() < 0) {// if trying to go down, set motor speed to
										// 0 (Not moving)
					winch.set(-0.1);
				} else {
					winch.set(stick2.getY() * -0.5);// otherwise go up
				}
			} else if (stick2.getRawButton(1)) {
				winch.set((stick2.getY() * -0.5) - 0.1);
			} else {
				winch.set((stick2.getY() * -winchMultiplier) - 0.1);
			}
		}

		if (stick2.getPOV(0) == 180 && cameraYPos > 0) {// down
			cameraYPos--;
		} else if (stick2.getPOV(0) == 0 && cameraYPos < 170) {// up
			cameraYPos++;
		} else if (stick2.getPOV(0) == 270 && cameraXPos > 0) {// left
			cameraXPos++;
		} else if (stick2.getPOV(0) == 90 && cameraXPos < 170) {// right
			cameraXPos--;
		} else if (stick2.getPOV(0) == 45 && cameraYPos < 170
				&& cameraXPos < 170) {// up, right
			cameraYPos++;
			cameraXPos--;
		} else if (stick2.getPOV(0) == 135 && cameraXPos < 170
				&& cameraYPos > 0) {// down, right
			cameraXPos--;
			cameraYPos--;
		} else if (stick2.getPOV(0) == 225 && cameraXPos > 0 && cameraYPos > 0) {// down,
																					// left
			cameraXPos++;
			cameraYPos--;
		} else if (stick2.getPOV(0) == 315 && cameraXPos > 0
				&& cameraYPos < 170) {// up, left
			cameraXPos++;
			cameraYPos++;
		} else if (stick2.getRawButton(1)) {
			cameraXPos = 85;
			cameraYPos = 160;

		}

		cameraX.setAngle(cameraXPos);
		cameraY.setAngle(cameraYPos);
		// System.out.println("X: " + cameraXPos);
		// System.out.println("Y: " + cameraYPos);
		// CameraServer.getInstance().startAutomaticCapture();
		
		if (eDrop) {
			if (!winchbottomR.get() || !winchbottomL.get()) {
				winch.set(0);
			} else {
				winch.set(1);
			}
			
			
			eDropTime--;
			if (eDropTime <= 0) {
				eDrop = false;
				eDropTime = 40;
			}
		}

	}

	/**
	 * This function is called periodically during test mode
	 */
	public void testPeriodic() {
		LiveWindow.run();
		// System.out.println(stick2.getPOV(0));
	}

}
// RJ's commwent section'
//
// system.out.println("awwwww");
// Andrew's commwent section
/*
 * double Xsquared = stick.getX()*stick.getX();//squares X input of stick1
 * double Ysquared = stick.getY()*stick.getY();//squares y input of stick1
 * double mag = Math.sqrt(Xsquared+Ysquared);//pythag theorem to get magnitude
 * of result vector double radAngle = Math.atan2(stick.getY(),
 * stick.getX());//inverse tangent of two vectors to get angle of result double
 * rawAngle = radAngle * 57.2957795;//converts radian to degrees double angle =
 * 0; if(Double.isNaN(radAngle)){ if(stick.getY()<0){ angle = 0; } else{ angle =
 * 180; } } else{ if(stick.getX()>0){ if(stick.getY()<0){ angle = 90 - rawAngle;
 * } if(stick.getY()>0){ angle = 90 + rawAngle; } } if(stick.getX()<0){
 * if(stick.getY()<0){ angle = 270 + rawAngle; } if(stick.getY()>0){ angle = 270
 * - rawAngle; } } }//This comment section is the trig behind polar mecanum
 */
/*
 * double rawMag = (stick.getX()+stick.getY()*-1)/2; double mag = rawMag/5;
 * double angle = stick.getDirectionDegrees(); //if(stick.getRawButton(1))
 * if(stick.getY()<0){ myRobot.mecanumDrive_Polar(rawMag, angle+180,
 * stick2.getX()*-1); } if(stick.getX()<0){ myRobot.mecanumDrive_Polar(rawMag,
 * angle+180, stick2.getX()*-1); } else{ myRobot.mecanumDrive_Polar(rawMag,
 * angle, stick2.getX()*-1); }//old mecanum
 */

/*
 * if(!winchbottomL.get() || !winchbottomR.get() || gotBin){ gotBin = true;
 * if(autoLoopCounter<120){ winch.set(-0.1); myRobot.arcadeDrive(0.5,0); } else
 * if(autoLoopCounter>120 && autoLoopCounter<130){
 * //myRobot.arcadeDrive(0,-0.4); out.set(false); in.set(true); } else
 * if(autoLoopCounter>240 && autoLoopCounter<320){ winch.set(-0.5); } else
 * if(autoLoopCounter>320 && autoLoopCounter < 350){ winch.set(-0.1);
 * myRobot.arcadeDrive(0.6,0); } else if(autoLoopCounter>350 &&
 * autoLoopCounter<430 && winchbottomL.get() && winchbottomR.get()){
 * winch.set(0.2); out.set(true); in.set(false); } else if(autoLoopCounter>430
 * && autoLoopCounter<440){ in.set(true); out.set(false); } else
 * if(autoLoopCounter>450 && autoLoopCounter<530){ winch.set(-0.5);
 * myRobot.arcadeDrive(0, -0.2); } else if(autoLoopCounter>530 &&
 * autoLoopCounter < 1060){ winch.set(-0.1); myRobot.arcadeDrive(0.6,0); } else
 * if(autoLoopCounter>1060 && autoLoopCounter<1100 && winchbottomL.get() &&
 * winchbottomR.get()){ winch.set(0.2); } else if(autoLoopCounter>1100 &&
 * autoLoopCounter<1200){ myRobot.arcadeDrive(0,0); out.set(true);
 * in.set(false); } autoLoopCounter++; } else{ winch.set(0.3); }
 */// auto code (working?)