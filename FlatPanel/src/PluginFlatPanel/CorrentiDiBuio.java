package PluginFlatPanel;

import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.plugin.PlugIn;

public class CorrentiDiBuio implements PlugIn {

//	public int setup(String arg, ImagePlus imp) {
//		this.imp = imp;
//		return DOES_ALL;
//	} // setup

	public void run(String arg) {

//		// ========= APRO CARTELLA PER METTERE REPORT001 ==============================
//		IJ.showMessage("Dove vuoi salvare Report001?");
//		String pathReport = ReadData.openWindowReport();
//		ReadData.initLog(pathReport);
//		ReadData.appendLog(pathReport, "Funzione di conversione");
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
		ReadData.appendLog(pathReport, "CorrentiDiBuio");
		// === FACCIO APRIRE FINESTRA DI DIALOGO PER SELEZIONARE MACCHINA
		int width = 0;
		int height = 0;
		long offset = 0;
		float dimPixel = 0;
		double m = 0.0073;
		double q = -0.2767;
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
		IJ.showMessage("CORRENTI DI BUIO");
		IJ.showMessage("Selezionare l'immagine relativa alle correnti di buio");
		String path1 = ReadData.chooseImageRaw();
		ImagePlus imp1 = ReadData.openRaw(path1, width, height, offset);
		ReadData.convertImageToDose(imp1, m, q);
		imp1.show();
	//	ReadData.waitHere("width= " + width + " height= " + height + " offset= " + offset);

		double[] statImm1 = ReadData.ROI6cm(imp1, dimPixel, width, height);
		double valMed1 = statImm1[0];
		double valDevSt1 = statImm1[1];
		ReadData.appendLog(pathReport, "#B001# = Valore medio = " + valMed1);
		ReadData.appendLog(pathReport, "#B011# = Dev St = " + valDevSt1);
		imp1.close();

	}
}
