package PluginFlatPanel;

import java.awt.Rectangle;
import java.util.ArrayList;

import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.WindowManager;
import ij.io.OpenDialog;
import ij.plugin.PlugIn;

//******************************************************************************
//******************************************************************************
//
//	Per dubbi, domande, osservazioni, critiche, rivolgersi a:
//	Osvaldo Rampado
//	S.C.D.O. Fisica Sanitaria 1 
//	A.S.O. San Giovanni Battista di Torino
// 	Tel. 0116335373
//	e-mail: orampado@molinette.piemonte.it
//
//******************************************************************************
//******************************************************************************

public class BadPixel_NUOVO implements PlugIn {
	// fa riferimento al foglio AnalisiDegliArtefatti

	public String ExtractInfo(String HEADER, String ST1, String ST2) {
		int STlen = ST1.length();
		String INFO = "";
		if (HEADER != null) {
			int index1 = HEADER.indexOf(ST1);
			int index2 = HEADER.indexOf(ST2, index1);
			if (index1 >= 0 && index2 >= 0) {
				INFO = HEADER.substring(index1 + STlen, index2);
				INFO = INFO.trim();
			}
		}
		return INFO;
	}

	ArrayList<String> riassuntoBadPixel = new ArrayList<String>();
	int m, l, i, j, k;

	int n = 100;

	int profilo[] = new int[n];
	float medie;

	public void run(String arg) {
		
		
		int contatoreBad = 0;

		IJ.log("---------------------------------");
		IJ.log(ReadData.qui() + "START>");
		String[] aux5 = arg.split(";");
		for (String myArg : aux5)
			IJ.log("arg= " + myArg);
		IJ.log("---------------------------------");

		// ########### APRO CARTELLA PER METTERE REPORT001 ##########################
		String pathReport = "";
		if (arg == "") {
			IJ.showMessage("Dove vuoi salvare Report001?");
			pathReport = ReadData.openWindowReport();
			ReadData.initLog(pathReport);
		} else {
			pathReport = Prefs.get("FlatPanel.Report001", "") + "Report001.txt";
		}
		ReadData.appendLog(pathReport, "BadPixel");
		// ###########################################################################

//		// ========= APRO CARTELLA PER METTERE REPORT001 ==============================
//		IJ.showMessage("Dove vuoi salvare Report001?");
//		String pathReport = ReadData.openWindowReport();
//		ReadData.initLog(pathReport);
//		ReadData.appendLog(pathReport, "Bad Pixel");
//		// ================================================================

		// === FACCIO APRIRE FINESTRA DI DIALOGO PER SELEZIONARE MACCHINA

		int width = 0;
		int height = 0;
		long offset = 0;
		float dimPixel = 0;
		String stringona = "";

		if (arg == "") {
			long[] datiMacchina = ReadData.selectFlatPanel();
			width = (int) datiMacchina[0];
			height = (int) datiMacchina[1];
			offset = (long) datiMacchina[2];
			dimPixel = (float) datiMacchina[3];
		} else {
			stringona = Prefs.get("FlatPanel.myPref", ""); // <- legge stringona dalla stringa delle preferenze
			String[] valori = stringona.split(";");
			width = Integer.parseInt(valori[0]);
			height = Integer.parseInt(valori[1]);
			offset = Long.parseLong(valori[2]);
			dimPixel = Float.parseFloat(valori[3]);
		}

		dimPixel = dimPixel / 1000;
		// ==============================================================

		// ==== APRO IMMAGINE ======
		IJ.showMessage("Bad Pixel");
		IJ.showMessage("Selezionare l'immagine presa in 'funzione di conversione' con dose piu' vicina a 2,5 uGy");
		String path1 = ReadData.chooseImageRaw();
		ImagePlus imp = ReadData.openRaw(path1, width, height, offset);

		if (imp == null) {
			IJ.noImage();
			return;
		}

		// **************************************************************************

		// Accesso all'header dell'immagine

		String header = (String) imp.getProperty("Info");
		String imageNum = "";

//			// Estrazione delle dimensioni del pixel
//    		float DimPX;
//     	    String sDimPX = ExtractInfo(header,"Pixel Spacing:","\\");			// Serie
//			if (sDimPX!="") {DimPX=(float)Double.parseDouble(sDimPX);}
//      		else {DimPX=0.1f;}

		// **************************************************************************

		// Dimensioni della ROI
		float DimPX = (float) dimPixel;
		n = (int) (10 / DimPX);

		int base = imp.getWidth();
		int altezza = imp.getHeight();

		int DimOr = (int) ((base - n * 3) / (n));
		int DimVer = (int) ((altezza - n * 3) / (n));
		int mmarg = (base - DimOr * n) / 2;
		int lmarg = (altezza - DimVer * n) / 2;

		int mtot = DimOr;
		int ltot = DimVer;

		float[][] Media = new float[ltot][mtot];
		float[][][] totalData = new float[ltot * mtot][n][n];
		float LimInf = 0.0f;
		float LimSup = 0.0f;

		for (l = 0; l < ltot; l++) {
			for (m = 0; m < mtot; m++) {

				i = l * mtot + m;
				IJ.showStatus("" + i);

				IJ.makeRectangle(mmarg + m * n, lmarg + l * n, n, n);

				Rectangle r = imp.getProcessor().getRoi();

				// Ciclo di acquisizione dei profili
				medie = 0.0f;

				for (j = 0; j < n; j++) {
					// Crea il profilo alla desiderata altezza
					imp.getProcessor().getRow(mmarg + m * n, lmarg + l * n + j, profilo, n);

					// Valutazione valore medio della ROI
					for (k = 0; k < n; k++) {
						// totalData[i][j][k]=(float)Math.pow(2.7182818f,((-(float)profilo[k]+18077f)/2915f));
						// // Philips
						totalData[i][j][k] = (float) Math.pow(2.7182818f, (((float) profilo[k] - 862) / 463f)); // Kodak
						// totalData[i][j][k]=(float)profilo[k];
						medie = medie + totalData[i][j][k];
					}

				}

				Media[l][m] = (float) (medie / (n * n));

				// Valutazione eventuali bad pixels nella ROI

				LimInf = Media[l][m] - 0.2f * Media[l][m];
				LimSup = Media[l][m] + 0.2f * Media[l][m];

				for (j = 0; j < n; j++) {
					for (k = 0; k < n; k++) {

						if (totalData[i][j][k] < LimInf || totalData[i][j][k] > LimSup) {
							String appoggio = "_C001_ == Rilevato un Bad Pixel in posizione x: "
									+ IJ.d2s(mmarg + m * n + k) + " e y: " + IJ.d2s(lmarg + l * n + j);
							// riassuntoBadPixel.add(appoggio);

							ReadData.appendLog(pathReport, appoggio);
							
							contatoreBad = contatoreBad+1;

							// IJ.log("Rilevato un Bad Pixel in posizione x =" + IJ.d2s(mmarg + m * n + k) +
							// " e y ="
							// + IJ.d2s(lmarg + l * n + j));
						}
					}

				}

			}
		}
		
		if (contatoreBad == 0) {
			ReadData.appendLog(pathReport, "_C001_ == Nessun Bad Pixel -> CANCELLARE A MANO QUESTA RIGA SU EXCEL");
			
		}
		
		imp.close();
	}

}