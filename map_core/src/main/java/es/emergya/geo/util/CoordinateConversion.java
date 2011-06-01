package es.emergya.geo.util;

/*
 * Author: Sami Salkosuo, sami.salkosuo@fi.ibm.com
 *
 * (c) Copyright IBM Corp. 2007
 */

import java.util.Hashtable;
import java.util.Map;

public class CoordinateConversion {

	public CoordinateConversion() {

	}

	public double[] utm2LatLon(String UTM) {
		UTM2LatLon c = new UTM2LatLon();
		return c.convertUTMToLatLong(UTM);
	}

	public String latLon2UTM(double latitude, double longitude) {
		LatLon2UTM c = new LatLon2UTM();
		return c.convertLatLonToUTM(latitude, longitude);

	}

	private void validate(double latitude, double longitude) {
		if ((latitude < -90.0) || (latitude > 90.0) || (longitude < -180.0)
				|| (longitude >= 180.0))
			throw new IllegalArgumentException(
			"Legal ranges: latitude [-90,90], longitude [-180,180).");

	}

	public String latLon2MGRUTM(double latitude, double longitude) {
		LatLon2MGRUTM c = new LatLon2MGRUTM();
		return c.convertLatLonToMGRUTM(latitude, longitude);

	}

	public double[] mgrutm2LatLon(String MGRUTM) {
		MGRUTM2LatLon c = new MGRUTM2LatLon();
		return c.convertMGRUTMToLatLong(MGRUTM);
	}

	public double degreeToRadian(double degree) {
		return degree * Math.PI / 180;
	}

	public double radianToDegree(double radian) {
		return radian * 180 / Math.PI;
	}

	private double POW(double a, double b) {
		return Math.pow(a, b);
	}

	private double SIN(double value) {
		return Math.sin(value);
	}

	private double COS(double value) {
		return Math.cos(value);
	}

	private double TAN(double value) {
		return Math.tan(value);
	}

	private class LatLon2UTM {
		public String convertLatLonToUTM(double latitude, double longitude) {
			CoordinateConversion.this.validate(latitude, longitude);
			String UTM = "";

			this.setVariables(latitude, longitude);

			String longZone = this.getLongZone(longitude);
			LatZones latZones = new LatZones();
			String latZone = latZones.getLatZone(latitude);

			double _easting = this.getEasting();
			double _northing = this.getNorthing(latitude);

			UTM = longZone + " " + latZone + " " + ((int) _easting) + " "
			+ ((int) _northing);
			// UTM = longZone + " " + latZone + " " +
			// decimalFormat.format(_easting) +
			// " "+ decimalFormat.format(_northing);

			return UTM;

		}

		protected void setVariables(double latitude, double longitude) {
			latitude = CoordinateConversion.this.degreeToRadian(latitude);
			this.rho = this.equatorialRadius * (1 - this.e * this.e)
			/ CoordinateConversion.this.POW(1 - CoordinateConversion.this.POW(this.e * CoordinateConversion.this.SIN(latitude), 2), 3 / 2.0);

			this.nu = this.equatorialRadius
			/ CoordinateConversion.this.POW(1 - CoordinateConversion.this.POW(this.e * CoordinateConversion.this.SIN(latitude), 2), (1 / 2.0));

			double var1;
			if (longitude < 0.0)
				var1 = ((int) ((180 + longitude) / 6.0)) + 1;
			else
				var1 = ((int) (longitude / 6)) + 31;
			double var2 = (6 * var1) - 183;
			double var3 = longitude - var2;
			this.p = var3 * 3600 / 10000;

			this.S = this.A0 * latitude - this.B0 * CoordinateConversion.this.SIN(2 * latitude) + this.C0 * CoordinateConversion.this.SIN(4 * latitude)
			- this.D0 * CoordinateConversion.this.SIN(6 * latitude) + this.E0 * CoordinateConversion.this.SIN(8 * latitude);

			this.K1 = this.S * this.k0;
			this.K2 = this.nu * CoordinateConversion.this.SIN(latitude) * CoordinateConversion.this.COS(latitude) * CoordinateConversion.this.POW(this.sin1, 2) * this.k0
			* (100000000) / 2;
			this.K3 = ((CoordinateConversion.this.POW(this.sin1, 4) * this.nu * CoordinateConversion.this.SIN(latitude) * Math.pow(CoordinateConversion.this.COS(latitude),
					3)) / 24)
					* (5 - CoordinateConversion.this.POW(CoordinateConversion.this.TAN(latitude), 2) + 9 * this.e1sq
							* CoordinateConversion.this.POW(CoordinateConversion.this.COS(latitude), 2) + 4 * CoordinateConversion.this.POW(this.e1sq, 2)
							* CoordinateConversion.this.POW(CoordinateConversion.this.COS(latitude), 4))
							* this.k0
							* (10000000000000000L);

			this.K4 = this.nu * CoordinateConversion.this.COS(latitude) * this.sin1 * this.k0 * 10000;

			this.K5 = CoordinateConversion.this.POW(this.sin1 * CoordinateConversion.this.COS(latitude), 3)
			* (this.nu / 6)
			* (1 - CoordinateConversion.this.POW(CoordinateConversion.this.TAN(latitude), 2) + this.e1sq * CoordinateConversion.this.POW(CoordinateConversion.this.COS(latitude), 2))
			* this.k0 * 1000000000000L;

			this.A6 = (CoordinateConversion.this.POW(this.p * this.sin1, 6) * this.nu * CoordinateConversion.this.SIN(latitude) * CoordinateConversion.this.POW(CoordinateConversion.this.COS(latitude), 5) / 720)
			* (61 - 58 * CoordinateConversion.this.POW(CoordinateConversion.this.TAN(latitude), 2) + CoordinateConversion.this.POW(CoordinateConversion.this.TAN(latitude), 4)
					+ 270 * this.e1sq * CoordinateConversion.this.POW(CoordinateConversion.this.COS(latitude), 2) - 330 * this.e1sq
					* CoordinateConversion.this.POW(CoordinateConversion.this.SIN(latitude), 2)) * this.k0 * (1E+24);

		}

		protected String getLongZone(double longitude) {
			double longZone = 0;
			if (longitude < 0.0)
				longZone = ((180.0 + longitude) / 6) + 1;
			else
				longZone = (longitude / 6) + 31;
			String val = String.valueOf((int) longZone);
			if (val.length() == 1)
				val = "0" + val;
			return val;
		}

		protected double getNorthing(double latitude) {
			double northing = this.K1 + this.K2 * this.p * this.p + this.K3 * CoordinateConversion.this.POW(this.p, 4);
			if (latitude < 0.0)
				northing = 10000000 + northing;
			return northing;
		}

		protected double getEasting() {
			return 500000 + (this.K4 * this.p + this.K5 * CoordinateConversion.this.POW(this.p, 3));
		}

		// Lat Lon to UTM variables

		// equatorial radius
		double equatorialRadius = 6378137;

		// polar radius
		double polarRadius = 6356752.314;

		// flattening
		double flattening = 0.00335281066474748;// (equatorialRadius-polarRadius)/equatorialRadius;

		// inverse flattening 1/flattening
		double inverseFlattening = 298.257223563;// 1/flattening;

		// Mean radius
		double rm = CoordinateConversion.this.POW(this.equatorialRadius * this.polarRadius, 1 / 2.0);

		// scale factor
		double k0 = 0.9996;

		// eccentricity
		double e = Math.sqrt(1 - CoordinateConversion.this.POW(this.polarRadius / this.equatorialRadius, 2));

		double e1sq = this.e * this.e / (1 - this.e * this.e);

		double n = (this.equatorialRadius - this.polarRadius)
		/ (this.equatorialRadius + this.polarRadius);

		// r curv 1
		double rho = 6368573.744;

		// r curv 2
		double nu = 6389236.914;

		// Calculate Meridional Arc Length
		// Meridional Arc
		double S = 5103266.421;

		double A0 = 6367449.146;

		double B0 = 16038.42955;

		double C0 = 16.83261333;

		double D0 = 0.021984404;

		double E0 = 0.000312705;

		// Calculation Constants
		// Delta Long
		double p = -0.483084;

		double sin1 = 4.84814E-06;

		// Coefficients for UTM Coordinates
		double K1 = 5101225.115;

		double K2 = 3750.291596;

		double K3 = 1.397608151;

		double K4 = 214839.3105;

		double K5 = -2.995382942;

		double A6 = -1.00541E-07;

	}

	private class LatLon2MGRUTM extends LatLon2UTM {
		public String convertLatLonToMGRUTM(double latitude, double longitude) {
			CoordinateConversion.this.validate(latitude, longitude);
			String mgrUTM = "";

			this.setVariables(latitude, longitude);

			String longZone = this.getLongZone(longitude);
			LatZones latZones = new LatZones();
			String latZone = latZones.getLatZone(latitude);

			double _easting = this.getEasting();
			double _northing = this.getNorthing(latitude);
			Digraphs digraphs = new Digraphs();
			String digraph1 = digraphs.getDigraph1(Integer.parseInt(longZone),
					_easting);
			String digraph2 = digraphs.getDigraph2(Integer.parseInt(longZone),
					_northing);

			String easting = String.valueOf((int) _easting);
			if (easting.length() < 5)
				easting = "00000" + easting;
			easting = easting.substring(easting.length() - 5);

			String northing;
			northing = String.valueOf((int) _northing);
			if (northing.length() < 5)
				northing = "0000" + northing;
			northing = northing.substring(northing.length() - 5);

			mgrUTM = longZone + latZone + digraph1 + digraph2 + easting
			+ northing;
			return mgrUTM;
		}
	}

	private class MGRUTM2LatLon extends UTM2LatLon {
		public double[] convertMGRUTMToLatLong(String mgrutm) {
			double[] latlon = { 0.0, 0.0 };
			// 02CNR0634657742
			int zone = Integer.parseInt(mgrutm.substring(0, 2));
			String latZone = mgrutm.substring(2, 3);

			String digraph1 = mgrutm.substring(3, 4);
			String digraph2 = mgrutm.substring(4, 5);
			this.easting = Double.parseDouble(mgrutm.substring(5, 10));
			this.northing = Double.parseDouble(mgrutm.substring(10, 15));

			LatZones lz = new LatZones();
			double latZoneDegree = lz.getLatZoneDegree(latZone);

			double a1 = latZoneDegree * 40000000 / 360.0;
			double a2 = 2000000 * Math.floor(a1 / 2000000.0);

			Digraphs digraphs = new Digraphs();

			double digraph2Index = digraphs.getDigraph2Index(digraph2);

			double startindexEquator = 1;
			if ((1 + zone % 2) == 1)
				startindexEquator = 6;

			double a3 = a2 + (digraph2Index - startindexEquator) * 100000;
			if (a3 <= 0)
				a3 = 10000000 + a3;
			this.northing = a3 + this.northing;

			this.zoneCM = -183 + 6 * zone;
			double digraph1Index = digraphs.getDigraph1Index(digraph1);
			int a5 = 1 + zone % 3;
			double[] a6 = { 16, 0, 8 };
			double a7 = 100000 * (digraph1Index - a6[a5 - 1]);
			this.easting = this.easting + a7;

			this.setVariables();

			double latitude = 0;
			latitude = 180 * (this.phi1 - this.fact1 * (this.fact2 + this.fact3 + this.fact4)) / Math.PI;

			if (latZoneDegree < 0)
				latitude = 90 - latitude;

			double d = this._a2 * 180 / Math.PI;
			double longitude = this.zoneCM - d;

			if (this.getHemisphere(latZone).equals("S"))
				latitude = -latitude;

			latlon[0] = latitude;
			latlon[1] = longitude;
			return latlon;
		}
	}

	private class UTM2LatLon {
		double easting;

		double northing;

		int zone;

		String southernHemisphere = "ACDEFGHJKLM";

		protected String getHemisphere(String latZone) {
			String hemisphere = "N";
			if (this.southernHemisphere.indexOf(latZone) > -1)
				hemisphere = "S";
			return hemisphere;
		}

		public double[] convertUTMToLatLong(String UTM) {
			double[] latlon = { 0.0, 0.0 };
			String[] utm = UTM.split(" ");
			this.zone = 30; // Integer.parseInt(utm[0]);
			// String latZone = utm[1];
			this.easting = Double.parseDouble(utm[0]);
			this.northing = Double.parseDouble(utm[1]);
			// String hemisphere = getHemisphere(latZone);
			double latitude = 0.0;
			double longitude = 0.0;

			// if (hemisphere.equals("S"))
			// {
			// northing = 10000000 - northing;
			// }
			this.setVariables();
			latitude = 180 * (this.phi1 - this.fact1 * (this.fact2 + this.fact3 + this.fact4)) / Math.PI;

			// if (zone > 0)
			// {
			this.zoneCM = 6 * this.zone - 183.0;
			// }
			// else
			// {
			// zoneCM = 3.0;
			//
			// }

			longitude = this.zoneCM - this._a3;
			// if (hemisphere.equals("S"))
			// {
			// latitude = -latitude;
			// }

			latlon[0] = latitude;
			latlon[1] = longitude;
			return latlon;

		}

		protected void setVariables() {
			this.arc = this.northing / this.k0;
			this.mu = this.arc
			/ (this.a * (1 - CoordinateConversion.this.POW(this.e, 2) / 4.0 - 3 * CoordinateConversion.this.POW(this.e, 4) / 64.0 - 5 * CoordinateConversion.this.POW(
					this.e, 6) / 256.0));

			this.ei = (1 - CoordinateConversion.this.POW((1 - this.e * this.e), (1 / 2.0)))
			/ (1 + CoordinateConversion.this.POW((1 - this.e * this.e), (1 / 2.0)));

			this.ca = 3 * this.ei / 2 - 27 * CoordinateConversion.this.POW(this.ei, 3) / 32.0;

			this.cb = 21 * CoordinateConversion.this.POW(this.ei, 2) / 16 - 55 * CoordinateConversion.this.POW(this.ei, 4) / 32;
			this.cc = 151 * CoordinateConversion.this.POW(this.ei, 3) / 96;
			this.cd = 1097 * CoordinateConversion.this.POW(this.ei, 4) / 512;
			this.phi1 = this.mu + this.ca * CoordinateConversion.this.SIN(2 * this.mu) + this.cb * CoordinateConversion.this.SIN(4 * this.mu) + this.cc * CoordinateConversion.this.SIN(6 * this.mu)
			+ this.cd * CoordinateConversion.this.SIN(8 * this.mu);

			this.n0 = this.a / CoordinateConversion.this.POW((1 - CoordinateConversion.this.POW((this.e * CoordinateConversion.this.SIN(this.phi1)), 2)), (1 / 2.0));

			this.r0 = this.a * (1 - this.e * this.e)
			/ CoordinateConversion.this.POW((1 - CoordinateConversion.this.POW((this.e * CoordinateConversion.this.SIN(this.phi1)), 2)), (3 / 2.0));
			this.fact1 = this.n0 * CoordinateConversion.this.TAN(this.phi1) / this.r0;

			this._a1 = 500000 - this.easting;
			this.dd0 = this._a1 / (this.n0 * this.k0);
			this.fact2 = this.dd0 * this.dd0 / 2;

			this.t0 = CoordinateConversion.this.POW(CoordinateConversion.this.TAN(this.phi1), 2);
			this.Q0 = this.e1sq * CoordinateConversion.this.POW(CoordinateConversion.this.COS(this.phi1), 2);
			this.fact3 = (5 + 3 * this.t0 + 10 * this.Q0 - 4 * this.Q0 * this.Q0 - 9 * this.e1sq)
			* CoordinateConversion.this.POW(this.dd0, 4) / 24;

			this.fact4 = (61 + 90 * this.t0 + 298 * this.Q0 + 45 * this.t0 * this.t0 - 252 * this.e1sq - 3
					* this.Q0 * this.Q0)
					* CoordinateConversion.this.POW(this.dd0, 6) / 720;

			//
			this.lof1 = this._a1 / (this.n0 * this.k0);
			this.lof2 = (1 + 2 * this.t0 + this.Q0) * CoordinateConversion.this.POW(this.dd0, 3) / 6.0;
			this.lof3 = (5 - 2 * this.Q0 + 28 * this.t0 - 3 * CoordinateConversion.this.POW(this.Q0, 2) + 8 * this.e1sq + 24 * CoordinateConversion.this.POW(
					this.t0, 2))
					* CoordinateConversion.this.POW(this.dd0, 5) / 120;
			this._a2 = (this.lof1 - this.lof2 + this.lof3) / CoordinateConversion.this.COS(this.phi1);
			this._a3 = this._a2 * 180 / Math.PI;

		}

		double arc;

		double mu;

		double ei;

		double ca;

		double cb;

		double cc;

		double cd;

		double n0;

		double r0;

		double _a1;

		double dd0;

		double t0;

		double Q0;

		double lof1;

		double lof2;

		double lof3;

		double _a2;

		double phi1;

		double fact1;

		double fact2;

		double fact3;

		double fact4;

		double zoneCM;

		double _a3;

		double b = 6356752.314;

		double a = 6378137;

		double e = 0.081819191;

		double e1sq = 0.006739497;

		double k0 = 0.9996;

	}

	private class Digraphs {
		private Map digraph1 = new Hashtable();

		private Map digraph2 = new Hashtable();

		private String[] digraph1Array = { "A", "B", "C", "D", "E", "F", "G",
				"H", "J", "K", "L", "M", "N", "P", "Q", "R", "S", "T", "U",
				"V", "W", "X", "Y", "Z" };

		private String[] digraph2Array = { "V", "A", "B", "C", "D", "E", "F",
				"G", "H", "J", "K", "L", "M", "N", "P", "Q", "R", "S", "T",
				"U", "V" };

		public Digraphs() {
			this.digraph1.put(new Integer(1), "A");
			this.digraph1.put(new Integer(2), "B");
			this.digraph1.put(new Integer(3), "C");
			this.digraph1.put(new Integer(4), "D");
			this.digraph1.put(new Integer(5), "E");
			this.digraph1.put(new Integer(6), "F");
			this.digraph1.put(new Integer(7), "G");
			this.digraph1.put(new Integer(8), "H");
			this.digraph1.put(new Integer(9), "J");
			this.digraph1.put(new Integer(10), "K");
			this.digraph1.put(new Integer(11), "L");
			this.digraph1.put(new Integer(12), "M");
			this.digraph1.put(new Integer(13), "N");
			this.digraph1.put(new Integer(14), "P");
			this.digraph1.put(new Integer(15), "Q");
			this.digraph1.put(new Integer(16), "R");
			this.digraph1.put(new Integer(17), "S");
			this.digraph1.put(new Integer(18), "T");
			this.digraph1.put(new Integer(19), "U");
			this.digraph1.put(new Integer(20), "V");
			this.digraph1.put(new Integer(21), "W");
			this.digraph1.put(new Integer(22), "X");
			this.digraph1.put(new Integer(23), "Y");
			this.digraph1.put(new Integer(24), "Z");

			this.digraph2.put(new Integer(0), "V");
			this.digraph2.put(new Integer(1), "A");
			this.digraph2.put(new Integer(2), "B");
			this.digraph2.put(new Integer(3), "C");
			this.digraph2.put(new Integer(4), "D");
			this.digraph2.put(new Integer(5), "E");
			this.digraph2.put(new Integer(6), "F");
			this.digraph2.put(new Integer(7), "G");
			this.digraph2.put(new Integer(8), "H");
			this.digraph2.put(new Integer(9), "J");
			this.digraph2.put(new Integer(10), "K");
			this.digraph2.put(new Integer(11), "L");
			this.digraph2.put(new Integer(12), "M");
			this.digraph2.put(new Integer(13), "N");
			this.digraph2.put(new Integer(14), "P");
			this.digraph2.put(new Integer(15), "Q");
			this.digraph2.put(new Integer(16), "R");
			this.digraph2.put(new Integer(17), "S");
			this.digraph2.put(new Integer(18), "T");
			this.digraph2.put(new Integer(19), "U");
			this.digraph2.put(new Integer(20), "V");

		}

		public int getDigraph1Index(String letter) {
			for (int i = 0; i < this.digraph1Array.length; i++)
				if (this.digraph1Array[i].equals(letter))
					return i + 1;

			return -1;
		}

		public int getDigraph2Index(String letter) {
			for (int i = 0; i < this.digraph2Array.length; i++)
				if (this.digraph2Array[i].equals(letter))
					return i;

			return -1;
		}

		public String getDigraph1(int longZone, double easting) {
			int a1 = longZone;
			double a2 = 8 * ((a1 - 1) % 3) + 1;

			double a3 = easting;
			double a4 = a2 + ((int) (a3 / 100000)) - 1;
			return (String) this.digraph1.get(new Integer((int) Math.floor(a4)));
		}

		public String getDigraph2(int longZone, double northing) {
			int a1 = longZone;
			double a2 = 1 + 5 * ((a1 - 1) % 2);
			double a3 = northing;
			double a4 = (a2 + ((int) (a3 / 100000)));
			a4 = (a2 + ((int) (a3 / 100000.0))) % 20;
			a4 = Math.floor(a4);
			if (a4 < 0)
				a4 = a4 + 19;
			return (String) this.digraph2.get(new Integer((int) Math.floor(a4)));

		}

	}

	private class LatZones {
		private char[] letters = { 'A', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K',
				'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Z' };

		private int[] degrees = { -90, -84, -72, -64, -56, -48, -40, -32, -24,
				-16, -8, 0, 8, 16, 24, 32, 40, 48, 56, 64, 72, 84 };

		private char[] negLetters = { 'A', 'C', 'D', 'E', 'F', 'G', 'H', 'J',
				'K', 'L', 'M' };

		private int[] negDegrees = { -90, -84, -72, -64, -56, -48, -40, -32,
				-24, -16, -8 };

		private char[] posLetters = { 'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V',
				'W', 'X', 'Z' };

		private int[] posDegrees = { 0, 8, 16, 24, 32, 40, 48, 56, 64, 72, 84 };

		private int arrayLength = 22;

		public LatZones() {
		}

		public int getLatZoneDegree(String letter) {
			char ltr = letter.charAt(0);
			for (int i = 0; i < this.arrayLength; i++)
				if (this.letters[i] == ltr)
					return this.degrees[i];
			return -100;
		}

		public String getLatZone(double latitude) {
			int latIndex = -2;
			int lat = (int) latitude;

			if (lat >= 0) {
				int len = this.posLetters.length;
				for (int i = 0; i < len; i++) {
					if (lat == this.posDegrees[i]) {
						latIndex = i;
						break;
					}

					if (lat > this.posDegrees[i])
						continue;
					else {
						latIndex = i - 1;
						break;
					}
				}
			} else {
				int len = this.negLetters.length;
				for (int i = 0; i < len; i++) {
					if (lat == this.negDegrees[i]) {
						latIndex = i;
						break;
					}

					if (lat < this.negDegrees[i]) {
						latIndex = i - 1;
						break;
					} else
						continue;

				}

			}

			if (latIndex == -1)
				latIndex = 0;
			if (lat >= 0) {
				if (latIndex == -2)
					latIndex = this.posLetters.length - 1;
				return String.valueOf(this.posLetters[latIndex]);
			} else {
				if (latIndex == -2)
					latIndex = this.negLetters.length - 1;
				return String.valueOf(this.negLetters[latIndex]);

			}
		}

	}

}
