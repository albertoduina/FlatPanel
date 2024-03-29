package PluginFlatPanel;

import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.plugin.PlugIn;

public class ImmaginiLatenti implements PlugIn {

//	public int setup(String arg, ImagePlus imp) {
//		this.imp = imp;
//		return DOES_ALL;
//	} // setup

	public void run(String arg) {

//		double m = 0.0073;
//		double q = -0.2767;
//
//		double[] valDose = { 0.5145, 1.0428, 2.1216, 4.2965, 8.6453, 17.3227 };
//
//		// ========= APRO CARTELLA PER METTERE REPORT001 ==============================
//		IJ.showMessage("Dove vuoi salvare Report001?");
//		String pathReport = ReadData.openWindowReport();
//		ReadData.initLog(pathReport);
//		ReadData.appendLog(pathReport, "Immagini latenti");
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
		ReadData.appendLog(pathReport, "ImmaginiLatenti");
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

		

		// ==== EFFETTO ADDITIVO ======
		IJ.showMessage("IMMAGINI LATENTI - effetto additivo");
		IJ.showMessage("EFFETTO ADDITIVO\nSelezionare l'immagine con l'oggetto");
		String path1 = ReadData.chooseImageRaw();
		ImagePlus imp1 = ReadData.openRaw(path1, width, height, offset);
		ReadData.convertImageToDose(imp1, m, q);

		double[] additivoOggetto = ReadData.meanAndDevStCircle(imp1, "Posiziona il cerchio su oggetto, POI clicca OK",
				null);
		int[] roiOggettoAdditivo = { (int) Math.round(additivoOggetto[2]), (int) Math.round(additivoOggetto[3]),
				(int) Math.round(additivoOggetto[4]) };
		ReadData.appendLog(pathReport, "#G001# = Media oggetto additivo = " + additivoOggetto[0]);
		// ReadData.appendLog(pathReport, "#Q401# = Dev st oggetto additivo = " +
		// additivoOggetto[1]);

		double[] additivoFondo = ReadData.meanAndDevStCircle(imp1,
				"Posiziona il cerchio sul fondo\n(fuori dall'oggetto), POI clicca OK", null);
		int[] roiFondoAdditivo = { (int) Math.round(additivoFondo[2]), (int) Math.round(additivoFondo[3]),
				(int) Math.round(additivoFondo[4]) };
		ReadData.appendLog(pathReport, "#G002# = Media fondo additivo = " + additivoFondo[0]);
		// ReadData.appendLog(pathReport, "#Q401# = Dev st fondo additivo = " +
		// additivoFondo[1]);

		imp1.close();
		
		IJ.showMessage("EFFETTO ADDITIVO\nSelezionare l'immagine non irradiata (buio)");
		String path2 = ReadData.chooseImageRaw();
		ImagePlus imp2 = ReadData.openRaw(path2, width, height, offset);
		ReadData.convertImageToDose(imp2, m, q);

		double[] additivoOggettoBuio = ReadData.meanAndDevStCircle(imp2, null, roiOggettoAdditivo);
		ReadData.appendLog(pathReport, "#G003# = Media buio oggetto additivo = " + additivoOggettoBuio[0]);
		// ReadData.appendLog(pathReport, "#Q601# = Dev St buio oggetto additivo = " +
		// additivoOggettoBuio[1]);

		double[] additivoFondoBuio = ReadData.meanAndDevStCircle(imp2, null, roiFondoAdditivo);
		ReadData.appendLog(pathReport, "#G004# = Media buio fondo additivo = " + additivoFondoBuio[0]);
		// ReadData.appendLog(pathReport, "#Q601# = Dev St buio fondo additivo = " +
		// additivoFondoBuio[1]);

		
		imp2.close();

		// ==== EFFETTO MOLTIPLICATIVO ======
		IJ.showMessage("IMMAGINI LATENTI - effetto moltiplicativo");
		IJ.showMessage("EFFETTO MOLTIPLICATIVO\nSelezionare l'immagine con l'oggetto (su excel e' la seconda)");
		String path3 = ReadData.chooseImageRaw();
		ImagePlus imp3 = ReadData.openRaw(path3, width, height, offset);
		ReadData.convertImageToDose(imp3, m, q);
		
		double[] moltiplicativoOggetto = ReadData.meanAndDevStCircle(imp3, "Posiziona il cerchio su oggetto, POI clicca OK",
				null);
		int[] roiOggettoMoltiplicativo = { (int) Math.round(moltiplicativoOggetto[2]), (int) Math.round(moltiplicativoOggetto[3]),
				(int) Math.round(moltiplicativoOggetto[4]) };
		ReadData.appendLog(pathReport, "#G007# = Media oggetto moltiplicativo = " + moltiplicativoOggetto[0]);

		double[] moltiplicativoFondo = ReadData.meanAndDevStCircle(imp3,
				"Posiziona il cerchio sul fondo\n(fuori dall'oggetto), POI clicca OK", null);
		int[] roiFondoMoltiplicativo = { (int) Math.round(moltiplicativoFondo[2]), (int) Math.round(moltiplicativoFondo[3]),
				(int) Math.round(moltiplicativoFondo[4]) };
		ReadData.appendLog(pathReport, "#G008# = Media fondo moltiplicativo = " + moltiplicativoFondo[0]);
		
		imp3.close();
		
		//---
		
		IJ.showMessage("EFFETTO MOLTIPLICATIVO\nSelezionare la prima immagine uniforme (su excel e' la prima)");
		String path4 = ReadData.chooseImageRaw();
		ImagePlus imp4 = ReadData.openRaw(path4, width, height, offset);
		ReadData.convertImageToDose(imp4, m, q);
		
		double[] moltiplicativoOggettoUnif1 = ReadData.meanAndDevStCircle(imp4, null, roiOggettoMoltiplicativo);
		ReadData.appendLog(pathReport, "#G005# = Media unif1 oggetto moltiplicativo = " + moltiplicativoOggettoUnif1[0]);
		
		double[] moltiplicativoFondoUnif1 = ReadData.meanAndDevStCircle(imp4, null, roiFondoMoltiplicativo);
		ReadData.appendLog(pathReport, "#G006# = Media unif1 fondo moltiplicativo = " + moltiplicativoFondoUnif1[0]);
		
		imp4.close();
		
		//--
		
		IJ.showMessage("EFFETTO MOLTIPLICATIVO\nSelezionare la seconda immagine uniforme (su excel e' la terza)");
		String path5 = ReadData.chooseImageRaw();
		ImagePlus imp5 = ReadData.openRaw(path5, width, height, offset);
		ReadData.convertImageToDose(imp5, m, q);
		
		double[] moltiplicativoOggettoUnif2 = ReadData.meanAndDevStCircle(imp5, null, roiOggettoMoltiplicativo);
		ReadData.appendLog(pathReport, "#G009# = Media unif2 oggetto moltiplicativo = " + moltiplicativoOggettoUnif2[0]);
		
		double[] moltiplicativoFondoUnif2 = ReadData.meanAndDevStCircle(imp5, null, roiFondoMoltiplicativo);
		ReadData.appendLog(pathReport, "#G010# = Media unif2 fondo moltiplicativo = " + moltiplicativoFondoUnif2[0]);
		
		imp5.close();

	}
}
