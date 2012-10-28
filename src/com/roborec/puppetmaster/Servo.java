package com.roborec.puppetmaster;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public class Servo
{
	public enum Operand
	{
		ROTATE(1,"R"),
		SET_MIN_PULSE_WIDTH(2,"P"),
		SET_MAX_PULSE_WIDTH(3,"Q"),
		SET_MAX_VELOCITY(4,"O"),
		SET_VELOCITY_LIMIT(5,"V"),
		UNLIMIT_VELOCITY(6,"U"),
		SET_MIN_ANGLE(7,"A"),
		SET_MAX_ANGLE(8,"B"),
		SET_INVERT(9,"I"),
		SET_NORMALIZE(10,"N");
		
		private int id;
		private String command;
		
		private static final Map<Integer, Operand> lookupId = new HashMap<Integer, Operand>();
		private static final Map<String, Operand> lookupCommand = new HashMap<String, Operand>();

		Operand(int id, String command){
			this.id = id;
			this.command = command;
		}

		static {
			for(Operand d : EnumSet.allOf(Operand.class))
			{
				lookupId.put(d.getId(), d);
				lookupCommand.put(d.getCommand(), d);
			}
		}

		public int getId(){
			return id;
		}
		
		public String getCommand(){
			return command;
		}

		public static Operand get(int id){
			return lookupId.get(id);
		}
		
		public static Operand get(String command){
			return lookupCommand.get(command);
		}
	}
	
	private int id;
	private int angle;
	private int minAngle;
	private int maxAngle;
	
	private String pHead;
	
	public Servo(int id, int angle){
		this.id = id;
		this.angle = angle;
		pHead = PuppetMasterActivity.Operand.SERVO + ":" + id + ":";
	}
	
	public int getId(){
		return id;
	}
	
	// CURRENT HERE IN THE REFACTOR
	//================================================================
	// FORMAT ANGLE
	public String rotateTo(int angle){
		this.angle = angle;
		return  PuppetMasterActivity.Operand.SERVO + Operand.ROTATE.command + id + angle;
	}
	
	public int getAngle(){
		return angle;
	}
	
	public String setMinPulseWidth(int pulse){
		return  pHead + Operand.SET_MIN_PULSE_WIDTH.id + ":" + pulse;
	}
	
	public String setMaxPulseWidth(int pulse){
		return  pHead + Operand.SET_MAX_PULSE_WIDTH.id + ":" + pulse;
	}
	
	public String setMaxVelocity(int maxVelocity){
		return  pHead + Operand.SET_MAX_VELOCITY.id + ":" + maxVelocity;
	}
	
	public String setVelocityLimit(int velocity){
		return  pHead + Operand.SET_VELOCITY_LIMIT.id + ":" + velocity;
	}
	
	public String unlimitVelocity(){
		return  pHead + Operand.UNLIMIT_VELOCITY.id;
	}
	
	public String setMinAngle(int minAngle){
		this.minAngle = minAngle;
		return  pHead + Operand.SET_MIN_ANGLE.id + ":" + minAngle;
	}
	
	public String setMaxAngle(int maxAngle){
		this.maxAngle = maxAngle;
		return  pHead + Operand.SET_MAX_ANGLE.id + ":" + maxAngle;
	}
	
	public int getMinAngle(){
		return minAngle;
	}
	
	public int getMaxAngle(){
		return maxAngle;
	}
	
	public String setInvert(boolean invert){
		return  pHead + Operand.SET_INVERT.id + ":" + (invert?1:0);
	}
	
	public String setNormalize(boolean normalize){
		return  pHead + Operand.SET_NORMALIZE.id + ":" + (normalize?1:0);
		
	}
	
	/*
	public int getMaxPulseWidth(){
		
	}
	public int getMinPulseWidth(){
		
	}
	public int getMaxVelocity(){
		
	}
	public int getLimitVelocity(){
		
	}
	public boolean isVelocityLimited(){
		
	}
	public boolean isInvert(){
		
	}
	public boolean isNormalize(){
		
	}
	*/
}
