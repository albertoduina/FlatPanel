package PluginFlatPanel;

import java.util.ArrayList;

import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.gui.WaitForUserDialog;
import ij.io.DirectoryChooser;
import ij.io.OpenDialog;
import ij.measure.CurveFitter;
import ij.plugin.PlugIn;

public class FunzioneConversione implements PlugIn {

//	public int setup(String arg, ImagePlus imp) {
//		this.imp = imp;
//		return DOES_ALL;
//	} // setup

	public void run(String arg) {

//		double[] valDose = { 0.5145, 1.0428, 2.1216, 4.2965, 8.6453, 17.3227 };

		// ========= APRO CARTELLA PER METTERE REPORT001 ==============================
		// ##############################################################
		/// QUESTO E'IL PRIMO PLUGIN E SI COMPORTA DIVERSAMENTE E NON USA ReadData!
		String oldPathReport = Prefs.get("FlatPanel.Report001", ""); // <- uso "" per dire che voglio prendere
		// quell'indirizzo della cartella
		DirectoryChooser.setDefaultDirectory(oldPathReport); // <- dovrebbe fare aprire poi la finestra per scegliere il
		// file su indirizzo salvato in oldPathReport
		IJ.showMessage("Dove vuoi salvare Report001?");
		DirectoryChooser windowReport = new DirectoryChooser(""); // <- fa aprire finestra di dialogo per selezionare
		// cartella
		String pathFolderReport = windowReport.getDirectory(); // <- salva l'indirizzo che ho aperto in windowReport
		Prefs.set("FlatPanel.Report001", pathFolderReport); // <- mette il nuovo percorso in pref.txt
		String pathReport = pathFolderReport + "Report001.txt";
		ReadData.initLog(pathReport);
		ReadData.appendLog(pathReport, "Funzione di conversione");
		// ##############################################################

		// === FACCIO APRIRE FINESTRA DI DIALOGO PER SELEZIONARE MACCHINA
		long[] datiMacchina = ReadData.selectFlatPanel();
		int width = (int) datiMacchina[0];
		int height = (int) datiMacchina[1];
		long offset = datiMacchina[2];
		int dimPixel = (int) datiMacchina[3];
		// per semplificare le cose riunisco tutta la rumenta da passare agli altri
		// plugin pel progetto FlatPanel in una unica stringa che alla fine scrivero'
		// nel file pref.txt
		String stringona = width + ";" + height + ";" + offset + ";" + dimPixel;

		// ==============================================================

		// ##############################################################
		// === LETTURA valDose DA FILE CSV RICEVUTO DA EXCEL
		IJ.showMessage("SELEZIONARE IL FILE CSV (funzioneconversioneCSV.csv)\nESPORTATO DA EXCEL CON I VALORI DI CONVERSIONE");
		OpenDialog od0 = new OpenDialog("SELEZIONARE funzioneconversioneCSV.csv", "");
		String path0 = od0.getPath();
		if (path0 == null)
			return;
		ArrayList<String> tabella = ReadData.readFileGeneric(path0);
		String[] tabellaBella = tabella.toArray(new String[tabella.size()]);
		if (tabellaBella.length != 6)
			ReadData.waitHere(
					"EHI TU ALLA TASTIERA TI TENGO D'OCCHIO, GUARDA CHE DEVO RICEVERE ESATTAMENTE SEI VALORI DI CONVERSIONE");
		// sostituisco eventuali virgole col punto
		for (int i1 = 0; i1 < tabellaBella.length; i1++) {
			tabellaBella[i1] = tabellaBella[i1].replace(",", ".");
		}
		// trasformo da stringa a double (ma non servirebbe, visto che lo scrivo solo in
		// stringona?)"
		double[] valDose = new double[tabellaBella.length];
		for (int i1 = 0; i1 < tabellaBella.length; i1++) {
			valDose[i1] = Double.parseDouble(tabellaBella[i1]);
		}
		// ##############################################################

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

		double vecMedieTutti[] = { valMed1, valMed2, valMed3, valMed4, valMed5, valMed6 };

		// Faccio fit con linea
		CurveFitter line = new CurveFitter(vecMedieTutti, valDose);
		line.doFit(CurveFitter.STRAIGHT_LINE); // 0 = STRAIGHT_LINE
		double[] param = line.getParams();

		double q = param[0];
		double m = param[1];

		new WaitForUserDialog("Pendenza retta: " + m + "\n" + "Intercetta: " + q).show();

		// ##############################################################
		//prendo i dati di inizio della macchina e ci aggiungo q e m
		
		stringona = stringona + ";" + m + ";" + q;
		Prefs.set("FlatPanel.myPref", stringona); // <- mette il valore di q nella stringa delle preferenze
		// ##############################################################

//		IJ.showMessage("APRI IMMAGINE A CASO");
//		String path7 = ReadData.chooseImageRaw();
//		ImagePlus imp7 = ReadData.openRaw(path7, width, height, offset);
//		ReadData.convertImageToDose(imp7, m, q);
//		ReadData.waitHere("CHIUDI IMMAGINE A CASO");

	}
}
