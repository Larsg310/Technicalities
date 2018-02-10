package com.technicalitiesmc.api.electricity;

/**
 * Created by Elec332 on 19-11-2017.
 *
 * Some AWG (American Wire Gauge) values
 */
public enum WireThickness {

	AWG_0000,
	AWG_000,
	AWG_00,
	AWG_0,  //4px
	AWG_1,
	AWG_2,
	AWG_3,
	AWG_4,
	AWG_5,
	AWG_6,
	AWG_7,  //3Px
	AWG_8,
	AWG_9,
	AWG_10,
	AWG_11,
	AWG_12,
	AWG_13,
	AWG_14,
	AWG_15, //2px
	AWG_16,
	AWG_17,
	AWG_18,
	AWG_19,
	AWG_20,
	AWG_21,
	AWG_22,
	AWG_23,
	AWG_24, //1px
	AWG_25,
	AWG_26,
	AWG_27,
	AWG_28,
	AWG_29;

	WireThickness(){
		//I didn't make this formula up, blame 'murica
		//To get the value in inches, devide by 200 instead of multiplying by 0.127
		diameter = 0.127 * Math.pow(92, (36 - (ordinal() - 3))/39);
		double radius = diameter / 2;
		surfaceAreaR = Math.PI * radius * radius;
		surfaceAreaS = diameter * diameter;
	}

	//In mm
	public final double diameter;
	public final double surfaceAreaR, surfaceAreaS;

}
