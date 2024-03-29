package PluginFlatPanel;

import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.gui.WaitForUserDialog;
import ij.plugin.PlugIn;

public class BassoContrasto implements PlugIn {
	// fa riferimento al foglio Sensibilita'aBassoContrasto

//	public int setup(String arg, ImagePlus imp) {
//		this.imp = imp;
//		return DOES_ALL;
//	} // setup

	public void run(String arg) {

//		double[] valDose = { 0.5145, 1.0428, 2.1216, 4.2965, 8.6453, 17.3227 };
//		
//		double m = 0.0073;
//		double q = -0.2767;
//
//		// ========= APRO CARTELLA PER METTERE REPORT001 ==============================
//		IJ.showMessage("Dove vuoi salvare Report001?");
//		String pathReport = ReadData.openWindowReport();
//		ReadData.initLog(pathReport);
//		ReadData.appendLog(pathReport, "Sensiblita' basso contrasto");
//		// ================================================================
//
//		// === FACCIO APRIRE FINESTRA DI DIALOGO PER SELEZIONARE MACCHINA
//		long[] datiMacchina = ReadData.selectFlatPanel();
//		int width = (int) datiMacchina[0];
//		int height = (int) datiMacchina[1];
//		long offset = datiMacchina[2];
//		int dimPixel = (int) datiMacchina[3];
//		// ==============================================================
		
		IJ.log("---------------------------------");
		IJ.log(ReadData.qui() + "START>");
		String[] aux5 = arg.split(";");
		for (String myArg : aux5) 
			IJ.log("arg= "+myArg);
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
		ReadData.appendLog(pathReport, "BassoContrasto");
		
		int width = 0;
		int height = 0;
		long offset = 0;
		float dimPixel = 0;
		double m=0;
		double q=0;
		String stringona = "";

		if (arg == "") {
			long[] datiMacchina = ReadData.selectFlatPanel();
			width = (int) datiMacchina[0];
			height = (int) datiMacchina[1];
			offset = datiMacchina[2];
			dimPixel = (float) datiMacchina[3];
		} else {
			stringona = Prefs.get("FlatPanel.myPref", ""); // <- legge stringona dalla stringa delle preferenze
			String[] valori = stringona.split(";");
			width = Integer.parseInt(valori[0]);
			height = Integer.parseInt(valori[1]);
			offset = Long.parseLong(valori[2]);
			dimPixel = Float.parseFloat(valori[3]);
			m = Double.parseDouble(valori[4]);
			q = Double.parseDouble(valori[5]);
		}
		
		// ###########################################################################
		
		
		
		

		// ==== APRO IMMAGINE ======
		IJ.showMessage("SENSIBILITA' BASSO CONTRASTO");
		IJ.showMessage("Selezionare immagine relativa a sensibilita' basso contrasto");
		String path1 = ReadData.chooseImageRaw();
		ImagePlus imp1 = ReadData.openRaw(path1, width, height, offset);
		ReadData.convertImageToDose(imp1, m, q);

		new WaitForUserDialog(
				"1) ZUMMARE IMMAGINE\n2) AGGIUSTARE IMMAGINE in\n'Image' - 'Adjust' - 'Brightness/Contrast'\nin modo che l'immagine si veda bene\nPOI premi OK")
						.show();

		// ==========================================================================
		//
		//
		// FINESTRA DI DIALOGO

		double[] valoriTabella1 = { 0.0532, 0.0354, 0.0282, 0.0188, 0.0136, 0.0095, 0.0076, 0.0056, 0.0038, 0.0026,
				0.0019, 0.0014 };

		double[] valoriTabella2 = { 0.0693, 0.0532, 0.0354, 0.0282, 0.0188, 0.0136, 0.0095, 0.0076, 0.0058, 0.0038,
				0.0026, 0.0019 };

		double[] valoriTabella3 = { 0.192, 0.134, 0.102, 0.0693, 0.0532, 0.0354, 0.0282, 0.0188, 0.0136, 0.0095, 0.0076,
				0.0058 };

		double[] valoriTabella4 = { 0.924, 0.655, 0.427, 0.298, 0.192, 0.134, 0.102, 0.0693, 0.0532, 0.0354, 0.0282,
				0.0188 };

		ReadData.radioButtonBassoContrasto(pathReport, "DETTAGLIO A - Quanti dischi vedi?", valoriTabella1,
				"#P101# = DETTAGLIO A numero cerchi = ", "#P201# = DETTAGLIO A contrasto = ");

		ReadData.radioButtonBassoContrasto(pathReport, "DETTAGLIO B - Quanti dischi vedi?", valoriTabella1,
				"#P102# = DETTAGLIO B numero cerchi = ", "#P202# = DETTAGLIO B contrasto = ");

		ReadData.radioButtonBassoContrasto(pathReport, "DETTAGLIO C - Quanti dischi vedi?", valoriTabella1,
				"#P103# = DETTAGLIO C numero cerchi = ", "#P203# = DETTAGLIO C contrasto = ");

		// ------

		ReadData.radioButtonBassoContrasto(pathReport, "DETTAGLIO D - Quanti dischi vedi?", valoriTabella2,
				"#P104# = DETTAGLIO D numero cerchi = ", "#P204# = DETTAGLIO D contrasto = ");

		ReadData.radioButtonBassoContrasto(pathReport, "DETTAGLIO E - Quanti dischi vedi?", valoriTabella2,
				"#P105# = DETTAGLIO E numero cerchi = ", "#P205# = DETTAGLIO E contrasto = ");

		ReadData.radioButtonBassoContrasto(pathReport, "DETTAGLIO F - Quanti dischi vedi?", valoriTabella2,
				"#P106# = DETTAGLIO F numero cerchi = ", "#P206# = DETTAGLIO F contrasto = ");

		// ------

		ReadData.radioButtonBassoContrasto(pathReport, "DETTAGLIO G - Quanti dischi vedi?", valoriTabella3,
				"#P107# = DETTAGLIO G numero cerchi = ", "#P207# = DETTAGLIO G contrasto = ");

		ReadData.radioButtonBassoContrasto(pathReport, "DETTAGLIO H - Quanti dischi vedi?", valoriTabella3,
				"#P108# = DETTAGLIO H numero cerchi = ", "#P208# = DETTAGLIO H contrasto = ");

		ReadData.radioButtonBassoContrasto(pathReport, "DETTAGLIO J - Quanti dischi vedi?", valoriTabella3,
				"#P109# = DETTAGLIO J numero cerchi = ", "#P209# = DETTAGLIO J contrasto = ");
		// ------

		ReadData.radioButtonBassoContrasto(pathReport, "DETTAGLIO K - Quanti dischi vedi?", valoriTabella4,
				"#P110# = DETTAGLIO K numero cerchi = ", "#P210# = DETTAGLIO K contrasto = ");

		ReadData.radioButtonBassoContrasto(pathReport, "DETTAGLIO L - Quanti dischi vedi?", valoriTabella4,
				"#P111# = DETTAGLIO L numero cerchi = ", "#P211# = DETTAGLIO L contrasto = ");

		ReadData.radioButtonBassoContrasto(pathReport, "DETTAGLIO M - Quanti dischi vedi?", valoriTabella4,
				"#P112# = DETTAGLIO M numero cerchi = ", "#P212# = DETTAGLIO M contrasto = ");
		
		double[] A1 = ReadData.meanAndDevStCircle(imp1,
				"Posiziona il cerchio sul dettaglio A1, POI clicca OK", null);
		ReadData.appendLog(pathReport, "#P301# = Media dettaglio A1 = " + A1[0]);
		
		double[] fondo = ReadData.meanAndDevStCircle(imp1,
				"Sposta la ROI al di fuori del cerchio, sul fondo, POI clicca OK", null);
		ReadData.appendLog(pathReport, "#P302# = Media fondo = " + fondo[0]);
		ReadData.appendLog(pathReport, "#P303# = Dev St fondo = " + fondo[1]);

		imp1.changes = false;
		imp1.close();

	}
}
