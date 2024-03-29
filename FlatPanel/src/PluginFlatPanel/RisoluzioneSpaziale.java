package PluginFlatPanel;

import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.gui.GenericDialog;
import ij.gui.WaitForUserDialog;
import ij.plugin.PlugIn;

public class RisoluzioneSpaziale implements PlugIn {
	// fa riferimento al foglio RisoluzioneSpazialeLimite


//	public int setup(String arg, ImagePlus imp) {
//		this.imp = imp;
//		return DOES_ALL;
//	} // setup

	public void run(String arg) {

//		double[] valDose = { 0.5145, 1.0428, 2.1216, 4.2965, 8.6453, 17.3227 };
//
//		// ========= APRO CARTELLA PER METTERE REPORT001 ==============================
//		IJ.showMessage("Dove vuoi salvare Report001?");
//		String pathReport = ReadData.openWindowReport();
//		ReadData.initLog(pathReport);
//		ReadData.appendLog(pathReport, "Risoluzione spaziale");
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
		ReadData.appendLog(pathReport, "RisoluzioneSpaziale");
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
		IJ.showMessage("RISOLUZIONE SPAZIALE");
		IJ.showMessage("Selezionare immagine relativa a risoluzione spaziale");
		String path1 = ReadData.chooseImageRaw();
		ImagePlus imp1 = ReadData.openRaw(path1, width, height, offset);

		// --- 1� mira
		new WaitForUserDialog("Zoomare su una delle due mire nell'angolo, POI cliccare su OK").show();

		float lpmm1 = (float) 3.55;

		GenericDialog gd1 = new GenericDialog("Quale gruppo di linee vedi?");
		gd1.addNumericField(
				"Quale gruppo di linee vedi?\nIndicare il numero di linee/mm mettendo come separatore decimale il PUNTO e non la virgola:",
				lpmm1);
		gd1.showDialog();

		lpmm1 = (float) gd1.getNextNumber();
		ReadData.appendLog(pathReport, "#E001# = Risoluzione angolo 1 = " + lpmm1);

		// --- 2� mira
		new WaitForUserDialog("Zoomare sulla mira nell'angolo rimanente, POI cliccare su OK").show();

		float lpmm2 = (float) 3.55;

		GenericDialog gd2 = new GenericDialog("Quale gruppo di linee vedi?");
		gd2.addNumericField(
				"Quale gruppo di linee vedi?\nIndicare il numero di linee/mm mettendo come separatore decimale il PUNTO e non la virgola:",
				lpmm2);
		gd2.showDialog();

		lpmm2 = (float) gd2.getNextNumber();
		ReadData.appendLog(pathReport, "#E003# = Risoluzione angolo 2 = " + lpmm2);

		// --- 3� mira
		new WaitForUserDialog("Zoomare sulla mira al centro, POI cliccare su OK").show();

		float lpmm3 = (float) 3.55;

		GenericDialog gd3 = new GenericDialog("Quale gruppo di linee vedi?");
		gd3.addNumericField(
				"Quale gruppo di linee vedi?\nIndicare il numero di linee/mm mettendo come separatore decimale il PUNTO e non la virgola:",
				lpmm3);
		gd3.showDialog();

		lpmm3 = (float) gd3.getNextNumber();
		ReadData.appendLog(pathReport, "#E002# = Risoluzione centro = " + lpmm3);
		
		
		imp1.close();

	}
}
