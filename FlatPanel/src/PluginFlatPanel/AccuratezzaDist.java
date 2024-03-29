package PluginFlatPanel;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.CurveFitter;
import ij.plugin.PlugIn;

public class AccuratezzaDist implements PlugIn {

//	public int setup(String arg, ImagePlus imp) {
//		this.imp = imp;
//		return DOES_ALL;
//	} // setup

	public void run(String arg) {

		double[] valDose = { 0.5145, 1.0428, 2.1216, 4.2965, 8.6453, 17.3227 };

		// ========= APRO CARTELLA PER METTERE REPORT001 ==============================
		IJ.showMessage("Dove vuoi salvare Report001?");
		String pathReport = ReadData.openWindowReport();
		ReadData.initLog(pathReport);
		ReadData.appendLog(pathReport, "Funzione di conversione");
		// ================================================================

		// === FACCIO APRIRE FINESTRA DI DIALOGO PER SELEZIONARE MACCHINA
		long[] datiMacchina = ReadData.selectFlatPanel();
		int width = (int) datiMacchina[0];
		int height = (int) datiMacchina[1];
		long offset = datiMacchina[2];
		int dimPixel = (int) datiMacchina[3];
		// ==============================================================

		// ==== APRO IMMAGINE ======
		IJ.showMessage("FUNZIONE DI CONVERSIONE");
		IJ.showMessage("Selezionare prima immagine della sezione funzione di conversione");
		String path1 = ReadData.chooseImageRaw();
		ImagePlus imp1 = ReadData.openRaw(path1, width, height, offset);

		double[] statImm1 = ReadData.ROI6cm(imp1, dimPixel, width, height);
		double valMed1 = statImm1[0];
		double valDevSt1 = statImm1[1];
		ReadData.appendLog(pathReport, "#A001# = Valore medio = " + valMed1);
		ReadData.appendLog(pathReport, "#A011# = Dev St = " + valDevSt1);
//		String valMed11 = Double.toString(valMed1);
//		ReadData.waitHere("Valore medio = " + valMed11);
		imp1.close();

		// ==== APRO IMMAGINE ======
		IJ.showMessage("Selezionare seconda immagine della sezione funzione di conversione");
		String path2 = ReadData.chooseImageRaw();
		ImagePlus imp2 = ReadData.openRaw(path2, width, height, offset);

		double[] statImm2 = ReadData.ROI6cm(imp2, dimPixel, width, height);
		double valMed2 = statImm2[0];
		double valDevSt2 = statImm2[1];
		ReadData.appendLog(pathReport, "#A002# = Valore medio = " + valMed2);
		ReadData.appendLog(pathReport, "#A012# = Dev St = " + valDevSt2);
		imp2.close();

		// ==== APRO IMMAGINE ======
		IJ.showMessage("Selezionare terza immagine della sezione funzione di conversione");
		String path3 = ReadData.chooseImageRaw();
		ImagePlus imp3 = ReadData.openRaw(path3, width, height, offset);

		double[] statImm3 = ReadData.ROI6cm(imp3, dimPixel, width, height);
		double valMed3 = statImm3[0];
		double valDevSt3 = statImm3[1];
		ReadData.appendLog(pathReport, "#A003# = Valore medio = " + valMed3);
		ReadData.appendLog(pathReport, "#A013# = Dev St = " + valDevSt3);
		imp3.close();

		// ==== APRO IMMAGINE ======
		IJ.showMessage("Selezionare quarta immagine della sezione funzione di conversione");
		String path4 = ReadData.chooseImageRaw();
		ImagePlus imp4 = ReadData.openRaw(path4, width, height, offset);

		double[] statImm4 = ReadData.ROI6cm(imp4, dimPixel, width, height);
		double valMed4 = statImm4[0];
		double valDevSt4 = statImm4[1];
		ReadData.appendLog(pathReport, "#A004# = Valore medio = " + valMed4);
		ReadData.appendLog(pathReport, "#A014# = Dev St = " + valDevSt4);
		imp4.close();

		// ==== APRO IMMAGINE ======
		IJ.showMessage("Selezionare quinta immagine della sezione funzione di conversione");
		String path5 = ReadData.chooseImageRaw();
		ImagePlus imp5 = ReadData.openRaw(path5, width, height, offset);

		double[] statImm5 = ReadData.ROI6cm(imp5, dimPixel, width, height);
		double valMed5 = statImm5[0];
		double valDevSt5 = statImm5[1];
		ReadData.appendLog(pathReport, "#A005# = Valore medio = " + valMed5);
		ReadData.appendLog(pathReport, "#A015# = Dev St = " + valDevSt5);
		imp5.close();

		// ==== APRO IMMAGINE ======
		IJ.showMessage("Selezionare sesta immagine della sezione funzione di conversione");
		String path6 = ReadData.chooseImageRaw();
		ImagePlus imp6 = ReadData.openRaw(path6, width, height, offset);

		double[] statImm6 = ReadData.ROI6cm(imp6, dimPixel, width, height);
		double valMed6 = statImm6[0];
		double valDevSt6 = statImm6[1];
		ReadData.appendLog(pathReport, "#A006# = Valore medio = " + valMed6);
		ReadData.appendLog(pathReport, "#A016# = Dev St = " + valDevSt6);
		imp6.close();
		
		double vecMedieTutti[] = {valMed1, valMed2, valMed3, valMed4, valMed5, valMed6};
		
		
		//Faccio fit con linea
		CurveFitter line = new CurveFitter(vecMedieTutti, valDose);
		line.doFit(CurveFitter.STRAIGHT_LINE); //0 = STRAIGHT_LINE
		double[] param = line.getParams();
		
		double q = param[0];
		double m = param[1];
		
		ReadData.waitHere("Pendenza retta: " + m + "\n" + "Intercetta: " + q);
		
		IJ.showMessage("APRI IMMAGINE A CASO");
		String path7 = ReadData.chooseImageRaw();
		ImagePlus imp7 = ReadData.openRaw(path7, width, height, offset);
		ReadData.convertImageToDose(imp7, m, q);

		

	}
}
