package PluginFlatPanel;

import java.awt.Color;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Prefs;
import ij.gui.GenericDialog;
import ij.gui.ImageWindow;
import ij.gui.OvalRoi;
import ij.gui.Overlay;
import ij.gui.PointRoi;
import ij.gui.Roi;
import ij.gui.WaitForUserDialog;
import ij.io.DirectoryChooser;
import ij.io.FileInfo;
import ij.io.OpenDialog;
import ij.io.Opener;
import ij.plugin.ContrastEnhancer;
import ij.plugin.ImageCalculator;
import ij.plugin.Raw;
import ij.process.FloatProcessor;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import ij.util.DicomTools;

public class ReadData {

	public static String readDicomString(ImagePlus imp1, String tag) {

		String parameter = tag + " -- " + DicomTools.getTagName(tag) + ":" + DicomTools.getTag(imp1, tag);
		return (parameter);
	}

	/**
	 * legge un valore double da una stringa
	 * 
	 * @param s1 stringa input
	 * @return valore double letto in s1
	 */
	public static double readDouble(String s1) {
		double x = 0;
		try {
			x = Double.valueOf(s1);
			// x = (new Double(s1)).doubleValue();
		} catch (Exception e) {
			IJ.error(qui() + " readDouble >> invalid double number " + s1);
			// tolto il messaggio per evitare isterismi nell'utenza
		}
		return x;
	}

	/**
	 * legge un valore float da una stringa
	 * 
	 * @param s1 stringa input
	 * @return valore float letto in s1
	 */
	public static float readFloat(String s1) {
		float x = 0;
		try {
			x = Float.valueOf(s1);
//			x = (new Float(s1)).floatValue();
		} catch (Exception e) {
			IJ.error(qui() + " readFloat >> invalid integer number " + s1);
		}
		return x;
	}

	/**
	 * legge un valore integer da una stringa
	 * 
	 * @param s1 stringa input
	 * @return valore integer letto in s1
	 */
	public static int readInt(String s1) {
		int x = 0;
		try {
			x = Integer.valueOf(s1);
//			x = (new Integer(s1)).intValue();
		} catch (Exception e) {
			IJ.error(qui() + " readInt >> invalid integer number " + s1);
		}
		return x;
	}

	/**
	 * estrae una parte di parametro dicom costituito da una stringa multipla
	 * 
	 * @param s1     stringa multipla
	 * @param number selezione parte da restituire
	 * @return stringa con la parte selezionata
	 */
	public static String readSubstring(String s1, int number) {
		StringTokenizer st = new StringTokenizer(s1, "\\ ");
		int nTokens = st.countTokens();
		String substring = "ERROR";
		if (number > nTokens)
			return substring;
		else
			substring = st.nextToken();
		for (int i1 = 1; i1 < number; i1++) {
			substring = st.nextToken();
		}
		return substring;
	}

	/**
	 * legge e carica in memoria un file di testo.
	 * 
	 * @param fileName path del file
	 * @return
	 */
	public static ArrayList<String> readFileGeneric(String fileName) {

		ArrayList<String> matrixTable = new ArrayList<String>();
		try {
			BufferedReader br = null;
			String path = "";
			path = fileName;
			br = new BufferedReader(new FileReader(path));
			while (br.ready()) {
				String line = br.readLine();
				matrixTable.add(line);
			}
			br.close();
		} catch (Exception e) {
			ReadData.waitHere("readFilegeneric error <" + fileName + "> " + e.getMessage());
			return null;
		}
		return matrixTable;
	}

	/**
	 * arresta il programma in modeless, presentando nome file sorgente e numero di
	 * linea
	 */
	public static void waitHere() {
		new WaitForUserDialog("file=" + Thread.currentThread().getStackTrace()[2].getFileName() + " " + " line="
				+ Thread.currentThread().getStackTrace()[2].getLineNumber()).show();
	}

	/**
	 * arresta il programma in modeless, presentando nome file sorgente e numero di
	 * linea e la stringa ricevuta
	 * 
	 * @param str
	 */
	public static void waitHere(String str) {
		new WaitForUserDialog("file=" + Thread.currentThread().getStackTrace()[2].getFileName() + " " + " line="
				+ Thread.currentThread().getStackTrace()[2].getLineNumber() + "\n \n" + str).show();
	}

	/**
	 * permette di insereire file e numero di linea in una stringa
	 * 
	 * @return
	 */
	public static String qui() {
		String out = ("<" + Thread.currentThread().getStackTrace()[2].getClassName() + "."
				+ Thread.currentThread().getStackTrace()[2].getMethodName()) + " line= "
				+ Thread.currentThread().getStackTrace()[2].getLineNumber() + ">  ";
		return out;
	}

	/**
	 * Legge ricorsivamente la directory e relative sottodirectory copied from
	 * www.javapractices.com (Alex Wong)
	 *
	 * @param startingDir directory "radice"
	 * @return lista dei path dei file
	 */
	public static List<File> getFileListing(File startingDir) {
		List<File> result = new ArrayList<File>();
		File[] filesAndDirs = startingDir.listFiles();
		if (filesAndDirs == null) {
			IJ.log("filesAndDirs==null");
			return null;
		}
		List<File> filesDirs = Arrays.asList(filesAndDirs);
		if (filesDirs == null) {
			IJ.log("filesDirs==null");
			return null;
		}
		for (File file : filesDirs) {
			if (!file.isFile()) {
				// must be a directory
				// recursive call !!
				List<File> deeperList = getFileListing(file);
				result.addAll(deeperList);
			} else {
				if (isImage(file)) {
					result.add(file);
				}
			}
		}
		return result;
	}

	public static boolean isImage(String fileName1) {
		Opener o1 = new Opener();
		int type = o1.getFileType(fileName1);
		if (type == Opener.UNKNOWN || type == Opener.JAVA_OR_TEXT || type == Opener.ROI || type == Opener.TEXT)
			return false;
		else
			return true;
	}

	public static boolean isImage(File file1) {
		Opener o1 = new Opener();
		int type = o1.getFileType(file1.getPath());
		if (type == Opener.UNKNOWN || type == Opener.JAVA_OR_TEXT || type == Opener.ROI || type == Opener.TEXT)
			return false;
		else
			return true;
	}

	/**
	 * Cancella il file Report001 precedente se c'è
	 * 
	 * @param path
	 * @return
	 */

	public static boolean initLog(String path) {
		File f1 = new File(path);
		// waitHere("path= "+path);
		if (f1.exists()) {
			GenericDialog gd1 = new GenericDialog("REPORT");
			gd1.addMessage("OVVIAMENTE per proseguire devo cancellare il report precedente, PROCEDO ?");
			gd1.enableYesNoCancel("YES", "NO");
			gd1.hideCancelButton();
			gd1.showDialog();
			if (gd1.wasOKed()) {
				IJ.log("stiamo per cancellare " + path);
				f1.delete();
				// ACRinputOutput.purgeDirectory(f1);
			}
			if (gd1.wasCanceled())
				return false;
		}

		// ACRlog.waitHere("DELETE 002");
		// appendLog(path, "< calculated " + LocalDate.now() + " @ " + LocalTime.now() +
		// " >");
		// ACRlog.waitHere("DELETE 003");

		return true;
	}

	public static void purgeDirectory(File dir) {
//		ACRlog.waitHere("dir= " + dir);
		for (File file : dir.listFiles()) {
			if (file.isDirectory())
				purgeDirectory(file);
			file.delete();
		}
	}

	/**
	 * Questo scrive il file Report001 in maniera append (aggiunge in coda una linea
	 * senza cancellarlo)
	 * 
	 * @param completePath
	 * @param linea
	 */
	public static void appendLog(String completePath, String linea) {

		BufferedWriter out;
		try {
//			ReadData.waitHere("scrivo nel log ");
			out = new BufferedWriter(new FileWriter(completePath, true));
			out.write(linea);
			out.newLine();
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return;
	}

	public static Polygon selectionPointsClick(ImagePlus imp1, String messageLabel) {

		String oldTool = IJ.getToolName(); // <- quello che stava usando prima, lo salvo
		imp1.killRoi(); // <- tolgo tutte le roi che c'erano prima
		IJ.setTool("multi"); // <- quello che voglio usare 'multi' è quello corrispondente alle crocettine
		new WaitForUserDialog(messageLabel).show();
		// ButtonMessages.ModelessMsg(messageLabel, buttonLabel);
		if (imp1.getRoi() == null)
			return null;

		Polygon p1 = imp1.getRoi().getPolygon();

		IJ.setTool(oldTool);
		return p1;
	}

	public static String chopLastCharacter(String in1) {
		return in1.substring(0, in1.length() - 1);
	}

	public static String analyzeTagDicom(ImagePlus imp1, String tagDicom) {
		String valore = DicomTools.getTag(imp1, tagDicom);
		String valore1 = valore.replaceAll("\\s+", ""); // occorre eliminare gli eventuali spazi
		String descrizione = DicomTools.getTagName(tagDicom);
		String stampa = descrizione + " = " + valore1;
		IJ.log(stampa);
		return stampa;
	}

	public static String analyzeDAPTagDicom(ImagePlus imp1) {
		String valore = DicomTools.getTag(imp1, "0018,115E"); // <- prendo dati DAP
		String valore1 = valore.replaceAll("\\s+", ""); //
		Double stampa55 = Double.parseDouble(valore1) * 10;
		String descrizione = DicomTools.getTagName("0018,115E");
		String stampa = descrizione + " = " + stampa55;
		IJ.log(stampa);
		return stampa;
	}

	// Apre la prima immagine presente nella cartella
	public static ImagePlus imageOpenFolder(String path) {
		File start1 = new File(path);
		List<File> pippo = ReadData.getFileListing(start1);
		File[] dataName = new File[pippo.size()];
		for (int i = 0; i < pippo.size(); i++) {
			dataName[i] = pippo.get(i);
		}
		String path1 = dataName[0].getPath(); // <- in realtà apro il primo file immagine che trova dentro la cartella
		String nameFile = dataName[0].getName();
		IJ.log("Nome file " + nameFile);
		Opener opener = new Opener();
		ImagePlus imp1 = opener.openImage(path1);
		if (imp1 == null) {
			ReadData.waitHere("L'immagine non si apre correttamente");
			return null;
		}
		return imp1;
	}

	public static ImagePlus imageFromStack(ImagePlus stack, int slice) {

		if (stack == null) {
			IJ.log("imageFromStack.stack== null");
			return null;
		}
		// IJ.log("stack bitDepth= "+stack.getBitDepth());
		ImageStack imaStack = stack.getImageStack();
		if (imaStack == null) {
			IJ.log("imageFromStack.imaStack== null");
			return null;
		}
		if (slice == 0) {
			IJ.log("imageFromStack.requested slice 0!");
			return null;

		}
		if (slice > stack.getStackSize()) {
			IJ.log("imageFromStack.requested slice > slices!");
			return null;
		}

		ImageProcessor ipStack = imaStack.getProcessor(slice);

		String titolo = "** " + slice + " **";
		String header = stack.getInfoProperty();
		// String titolo = imaStack.getShortSliceLabel(slice);
		// String sliceInfo1 = imaStack.getSliceLabel(slice);

		ImagePlus imp = new ImagePlus(titolo, ipStack);
		imp.setProperty("Info", header);
		return imp;
	}

	/**
	 * Zoom max immagine
	 *
	 * @param imp1 immagine di input
	 */
	public static void zoom(ImagePlus imp1) {
		imp1 = IJ.getImage();
		ImageWindow win = imp1.getWindow();
		// double magnification = win.getCanvas().getMagnification();
		win.maximize();
		return;
	}

	/**
	 * Calcolo dell'angolo di una linea di cui conosciamo due punti. Non utilizza
	 * Line di ImageJ, poiche'quest'ultimo passa attraverso gli integer
	 *
	 * @param ax
	 * @param ay
	 * @param bx
	 * @param by
	 * @return
	 */
	public static double getAngle(double ax, double ay, double bx, double by) {

		return -Math.toDegrees(Math.atan2(ay - by, ax - bx));
	}

	/**
	 * calcoli per rototraslazione delle coordinate rispetto ad un segmento di
	 * riferimento
	 *
	 * @param ax     coord x inizio segmento di riferimento
	 * @param ay     coord y inizio segmento di riferimento
	 * @param bx     coord x fine segmento di riferimento
	 * @param by     coord y fine segmento di riferimento
	 * @param cx     coordinata x da rototraslare
	 * @param cy     coordinata y da rototraslare
	 * @param debug1 true per debug
	 * @return msd[0] coordinata x rototraslata, msd[1] coordinata y rototraslata
	 */
	public static double[] coord2D(double ax, double ay, double bx, double by, double cx, double cy, boolean debug1) {
		double x1;
		double y1;
		double msd[];

		double angle1 = Math.atan((ay - by) / (ax - bx));
		if (debug1) {
			IJ.log("-------- coord2D --------");

			IJ.log("angolo= " + Math.toDegrees(angle1) + "  sin= " + Math.sin(angle1));
			IJ.log("angolo= " + Math.toDegrees(angle1) + "  cos= " + Math.cos(angle1));
		}
		if (Math.sin(angle1) < 0) {
			// piu30
			x1 = -cx * Math.sin(angle1) - cy * Math.cos(angle1) + ax;
			y1 = cx * Math.cos(angle1) - cy * Math.sin(angle1) + ay;
		} else {
			// meno30
			x1 = cx * Math.sin(angle1) + cy * Math.cos(angle1) + ax;
			y1 = -cx * Math.cos(angle1) + cy * Math.sin(angle1) + ay;
		}
		msd = new double[2];
		msd[0] = x1;
		msd[1] = y1;
		if (debug1) {
			IJ.log("coord2D output cx= " + cx + "  x1= " + x1 + "  cy= " + cy + "  y1= " + y1);
			IJ.log("--------------------------");

		}
		return msd;
	} // coord2D

	public static double[] coord2D2(double[] vetReference, double cx, double cy, boolean debug1) {
		double x1;
		double y1;
		double msd[];

		double angle1 = Math.atan((vetReference[1] - vetReference[3]) / (vetReference[0] - vetReference[2]));
		// double decimalAngle = Math.toDegrees(angle1);
		// IJ.log("decimalAngle= "+IJ.d2s(decimalAngle,3));

		if (debug1) {
			IJ.log("-------- coord2D2 --------");

			IJ.log("angolo= " + Math.toDegrees(angle1) + "  sin= " + Math.sin(angle1));
			IJ.log("angolo= " + Math.toDegrees(angle1) + "  cos= " + Math.cos(angle1));
		}

		if (Math.sin(angle1) < 0) {
			// piu30
			x1 = -cx * Math.sin(angle1) - cy * Math.cos(angle1) + vetReference[0];
			y1 = cx * Math.cos(angle1) - cy * Math.sin(angle1) + vetReference[1];
		} else {
			// meno30
			x1 = cx * Math.sin(angle1) + cy * Math.cos(angle1) + vetReference[0];
			y1 = -cx * Math.cos(angle1) + cy * Math.sin(angle1) + vetReference[1];
		}
		msd = new double[2];
		msd[0] = x1;
		msd[1] = y1;
		if (debug1) {
			IJ.log("coord2D output cx= " + cx + "  x1= " + x1 + "  cy= " + cy + "  y1= " + y1);
			IJ.log("--------------------------");
		}

		return msd;
	} // coord2D

	/**
	 * dati due punti di un segmento inclinato ed una distanza, traccia un segmento
	 * parallelo, restituendo gli offset da applicare ai punti del segmento per
	 * traslarlo correttamente USATO
	 *
	 * @param punti
	 * @param distanza
	 * @return
	 */
	public static double[] parallela(double[][] punti, double distanza) {
		double ax = punti[0][2];
		double ay = punti[1][2];
		double bx = punti[0][3];
		double by = punti[1][3];
		double slope = (ax - bx) / (ay - by);
		double perpendicolar = -1 / slope;
		double sinperpendicolar = perpendicolar / Math.sqrt(1 + perpendicolar * perpendicolar);
		double cosperpendicolar = 1 / Math.sqrt(1 + perpendicolar * perpendicolar);
		// data l'ipotenusa, la distanza perpendicolare tra i due segmenti, devo trovare
		// i due cateti, che sono gli spostamenti X ed Y per i punti del segmento
		// parallelo
		double xoffset = distanza * sinperpendicolar;
		double yoffset = distanza * cosperpendicolar;

		if (true) {
			IJ.log(ReadData.qui() + "input a= " + ax + " , " + ay + " b= " + bx + " , " + by);
			IJ.log(ReadData.qui() + "slope= " + slope);
			IJ.log(ReadData.qui() + "perp= " + perpendicolar);
			IJ.log(ReadData.qui() + "sinperp= " + sinperpendicolar);
			IJ.log(ReadData.qui() + "cosperp= " + cosperpendicolar);
			IJ.log(ReadData.qui() + "xoffset= " + xoffset);
			IJ.log(ReadData.qui() + "yoffset= " + yoffset);
		}

		double[] vetout = new double[2];
		vetout[0] = xoffset;
		vetout[1] = yoffset;
		return vetout;
	}

	public static void plotPoints(ImagePlus imp1, Overlay over1, int xPoints1, int yPoints1, Color color, int type,
			int size) {

		float[] xPoints = new float[1];
		float[] yPoints = new float[1];

		xPoints[0] = (float) xPoints1;
		yPoints[0] = (float) yPoints1;
		// logVector(xPoints, "xPoints");
		// logVector(yPoints, "yPoints");
		// ACRutils.waitHere();

		PointRoi pr1 = new PointRoi(xPoints, yPoints, xPoints.length);
		pr1.setPointType(type);
		pr1.setSize(size);

		imp1.setRoi(pr1);
		imp1.getRoi().setStrokeColor(color);
		over1.addElement(imp1.getRoi());
		imp1.killRoi();
		return;
	}

	/**
	 * genera l'immagine differenza pixel-by-pixel
	 *
	 * @param imp1 immagine minuendo
	 * @param imp2 immagine sottraendo
	 * @return immagine differenza
	 */
	public static ImagePlus genImaDifference(ImagePlus imp1, ImagePlus imp2) {

		ImageProcessor ip1;
		ImageProcessor ip2;
		double v1, v2, v3;
		// boolean debug = false;
		// if (UtilAyv.compareImagesByPixel(imp1, imp2, debug))
		// MyLog.waitThere("ATTENZIONE SONO STATE PASSATE A GENIMADIFFERENCE \n"
		// + "DUE IMMAGINU UGUALI, L'IMMAGINE DIFFERENZA VARRA' \n"
		// + "PERTANTO ZERO E SI AVRA' UN SNR INFINITY");

		// if (imp1 == null)
		// return (null);
		// if (imp2 == null)
		// return (null);
		int width = imp1.getWidth();
		int height = imp1.getHeight();
		float[] pixels = new float[width * height];
		ip1 = imp1.getProcessor();
		ip2 = imp2.getProcessor();
		ImageProcessor ip3 = new FloatProcessor(width, height, pixels, null);
		ImagePlus imp3 = new ImagePlus("Immagine differenza pixel-by-pixel", ip3);
		ip3 = imp3.getProcessor();
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				v1 = ip1.getPixelValue(x, y);
				v2 = ip2.getPixelValue(x, y);
				v3 = v1 - v2;
				ip3.putPixelValue(x, y, v3);
			}
		}
		return imp3;
	}

	/**
	 * genera l'immagine differenza pixel-by-pixel
	 *
	 * @param i1 immagine minuendo
	 * @param i2 immagine sottraendo
	 * @return immagine differenza
	 */
	public static ImagePlus diffIma(ImagePlus imp1, ImagePlus imp2) {

		ImageCalculator ic1 = new ImageCalculator();
		String params = "Subtract create 32-bit";
		ImagePlus imp3 = ic1.run(params, imp1, imp2);
		return imp3;
	}

	public static String analyzePulseRateTagDICOM(ImagePlus imp1) {

		String normal = " FL Neuro";
		String high = " FL(+) Neuro";
		String low = " FL(-)  Neuro";

		// String pippo = DicomTools.getTag(imp1, "0008,103E");
		// IJ.log(pippo);

		String valueModalita = DicomTools.getTag(imp1, "0008,103E");

		String stampa4 = ""; // perche' sia visibile al momento del return stampa4 deve essere definita qui,
		// prima ed al di fuori delle parentesi (sui manuali di java trovi la
		// descrizione di questo problema sotto "variable scope")

		// if (DicomTools.getTag(imp1, "0008,103E") == " FL Neuro") {
		// String stampa4 = "7.5";
		// IJ.log("PRIMA CICLO SUPER IF Pulse rate = " + stampa4);
		// ReadData.appendLog(pathReport, "#F007# = " + stampa4);
		// }

		if (DicomTools.getTag(imp1, "0018,0040") == null) {

			if (valueModalita.replaceAll("\\s+", "").equalsIgnoreCase(normal.replaceAll("\\s+", ""))) {
				stampa4 = "Pulse rate = 7.5";
				IJ.log(stampa4);
				// return stampa4;
			}
			if (valueModalita.replaceAll("\\s+", "").equalsIgnoreCase(high.replaceAll("\\s+", ""))) {
				stampa4 = "Pulse rate = 15";
				IJ.log(stampa4);
				// return stampa4;
			}
			if (valueModalita.replaceAll("\\s+", "").equalsIgnoreCase(low.replaceAll("\\s+", ""))) {
				stampa4 = "Pulse rate = 7.5";
				IJ.log(stampa4);
				// return stampa4;
			}

		} else {
			stampa4 = ReadData.analyzeTagDicom(imp1, "0018,0040");
			IJ.log(stampa4);
			// return stampa4;
		}

		return stampa4;

		// DEVO METTERE UN RETURN STAMPA 4

	}

	public static ImagePlus imageOpenAndShow(String pathFolderImm) {
		// ================= ESTRAGGO FILE IMMAGINE DA DENTRO LA CARTELLA ===========
		ImagePlus imp1 = ReadData.imageOpenFolder(pathFolderImm);
		if (imp1.getStackSize() > 1) { // <- se l'immagine è uno stack (contiene più di un'immagine)
			imp1 = ReadData.imageFromStack(imp1, 2); // <- seleziono ed apro solo la seconda immagine
		}
		// ===========================================================================
		//
		//
		// ==== UNA VOLTA APERTA LA CARTELLA MOSTRO L'IMAGINE E SALVO DATI HEADER ====
		new ContrastEnhancer().stretchHistogram(imp1.getProcessor(), 0.5); // <- per migliorare il contrasto
		imp1.show();
		ReadData.zoom(imp1);

		return imp1;

	}

	public static void analyzeALLTagDicom(ImagePlus imp1, String pathReport, String lettera, String numero) {

		String stampaName = ReadData.analyzeTagDicom(imp1, "0010,0010"); // <- prendo dati NOME
		ReadData.appendLog(pathReport, "#" + lettera + numero + "2# = " + stampaName);

		String leggiDate = ReadData.analyzeTagDicom(imp1, "0008,0023"); // <- prendo dati DATA
		String myDate = DicomTools.getTag(imp1, "0008,0023"); // <- prendo dati DATA --->>>>ma A MODO MIO !!!!!
		String anno = myDate.substring(1, 5);
		String mese = myDate.substring(5, 7);
		String giorno = myDate.substring(7, 9);
		String stampaDate = "Image Date= " + anno + "/" + mese + "/" + giorno;
//		String stampaDate = leggiDate;

//		ReadData.waitHere("myDayte= " + myDate + " anno= " + anno + " mese= " + mese + " giorno= " + giorno);

		ReadData.appendLog(pathReport, "#" + lettera + numero + "1# = " + stampaDate);

		String stampa1 = ReadData.analyzeTagDicom(imp1, "0018,0060"); // <- prendo dati TENSIONE
		ReadData.appendLog(pathReport, "#" + lettera + numero + "4# = " + stampa1);

		String stampa2 = ReadData.analyzeTagDicom(imp1, "0018,1151"); // <- prendo dati CORRENTE
		ReadData.appendLog(pathReport, "#" + lettera + numero + "5# = " + stampa2);

		String stampa3 = ReadData.analyzeTagDicom(imp1, "0018,1154"); // <- prendo dati DURATA ESPOSIZIONE (ms/frame)
		ReadData.appendLog(pathReport, "#" + lettera + numero + "6# = " + stampa3);

		// String stampa4 = ReadData.analyzePulseRateTagDICOM(imp1); // <- prendo dati
		// PULSE RATE
		// ReadData.appendLog(pathReport, "#" + lettera + numero + "7# = " + stampa4);

		return;
	}

	public static String openWindowReport() {
		String oldPathReport = Prefs.get("FlatPanel.Report001", ""); // <- uso "" per dire che voglio prendere
		// quell'indirizzo della cartella
		DirectoryChooser.setDefaultDirectory(oldPathReport); // <- dovrebbe fare aprire poi la finestra per scegliere il
		// file su indirizzo salvato in oldPathReport
		DirectoryChooser windowReport = new DirectoryChooser(""); // <- fa aprire finestra di dialogo per selezionare
		// cartella
		// DirectoryChooser.setDefaultDirectory(oldPathReport); // <- dovrebbe fare
		// aprire poi la finestra per scegliere il
		// file su indirizzo salvato in oldPathReport
		String pathFolderReport = windowReport.getDirectory(); // <- salva l'indirizzo che ho aperto in windowReport

		// if (pathFolderReport == null) { // <- perchè se non seleziona nessuna
		// cartella il programma si blocca
		// return;
		// }

		String pathReport = pathFolderReport + "Report001.txt";
		Prefs.set("FlatPanel.Report001", pathReport); // <- mette il nuovo percorso che ho aperto prima dentro il
		// file pref.txt

		return pathReport;
	}

	public static String openWindowImage() {
		String oldPathFolderImm = Prefs.get("angio.cartellaImm", "");
		DirectoryChooser.setDefaultDirectory(oldPathFolderImm); // <- dovrebbe fare aprire poi la finestra per scegliere
																// il file su indirizzo salvato in oldPathFolderImm
		DirectoryChooser windowFolderImm = new DirectoryChooser(""); // <- fa aprire finestra di dialogo per selezionare
																		// cartella
		String pathFolderImm = windowFolderImm.getDirectory(); // <- salva l'indirizzo che ho aperto in windowFolderImm
		// if (pathFolderImm == null)
		// return;

		Prefs.set("angio.cartellaImm", pathFolderImm); // <- mette il nuovo percorso che ho aperto prima dentro il
														// file pref.txt
		return pathFolderImm;
	}

	public static String openWindowImage(String msg) {
		String oldPathFolderImm = Prefs.get("angio.cartellaImm", "");
		DirectoryChooser.setDefaultDirectory(oldPathFolderImm); // <- dovrebbe fare aprire poi la finestra per scegliere
																// il file su indirizzo salvato in oldPathFolderImm
		DirectoryChooser windowFolderImm = new DirectoryChooser(msg); // <- fa aprire finestra di dialogo per
																		// selezionare
																		// cartella
		String pathFolderImm = windowFolderImm.getDirectory(); // <- salva l'indirizzo che ho aperto in windowFolderImm
		// if (pathFolderImm == null)
		// return;

		Prefs.set("angio.cartellaImm", pathFolderImm); // <- mette il nuovo percorso che ho aperto prima dentro il
														// file pref.txt
		return pathFolderImm;
	}

	public static void radioButtonBassoContrasto(String pathReport, String descrizioneFinestra, double[] valoriTabella,
			String numeroCerchiVisti, String valoreContrastoVisto) {

		GenericDialog gd = new GenericDialog("Soglia basso contrasto");
		String[] items = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12" };
		gd.addRadioButtonGroup(descrizioneFinestra, items, 4, 3, "1");
		gd.showDialog();

		String puntatore1 = gd.getNextRadioButton();

		int numeroDettagli = ReadData.readInt(puntatore1);
		double contrasto = valoriTabella[numeroDettagli - 1];

		// LEGGE LA FINESTRA DI DIALOGO
		IJ.log("Numero di cerchi: " + numeroDettagli);
		ReadData.appendLog(pathReport, numeroCerchiVisti + numeroDettagli);

		IJ.log("Numero di cerchi: " + contrasto);
		ReadData.appendLog(pathReport, valoreContrastoVisto + contrasto);

		gd.dispose();

		return;

	}

	public static void radioButtonRangeDinamico(String pathReport, String descrizioneFinestra, double[] valoriTabella,
			String numeroCerchiVisti, String valoreContrastoVisto) {

		GenericDialog gd = new GenericDialog("Soglia basso contrasto");
		String[] items = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16" };
		gd.addRadioButtonGroup(descrizioneFinestra, items, 4, 4, "1");
		gd.showDialog();

		String puntatore1 = gd.getNextRadioButton();

		int numeroDettagli = ReadData.readInt(puntatore1);
		double contrasto = valoriTabella[numeroDettagli - 1];

		// LEGGE LA FINESTRA DI DIALOGO
		IJ.log("Numero di cerchi: " + numeroDettagli);
		ReadData.appendLog(pathReport, numeroCerchiVisti + numeroDettagli);

		IJ.log("Numero di cerchi: " + contrasto);
		ReadData.appendLog(pathReport, valoreContrastoVisto + contrasto);

		gd.dispose();

		return;

	}

	// questa versione effettua posizionamento roi
	public static double[] meanAndDevStCircle(ImagePlus imp3, String descrizionePopUp, int[] roiData) {
		int larghezza = imp3.getWidth();
		int lunghezza = imp3.getHeight();

		Overlay over1 = new Overlay();
		imp3.setOverlay(over1);
		imp3.killRoi();

		double radius = 300;
		IJ.log(ReadData.qui() + " con " + imp3.getTitle() + " " + roiData);

		// se al posto dei dati roi viene passato null, allora la roi viene posizionata
		// di default al centro immagine
		if (roiData == null) {
			IJ.log(ReadData.qui() + "UNO" + " larghezza= " + larghezza + " lunghezza= " + lunghezza + " radius= "
					+ radius);
			imp3.setRoi(new OvalRoi(larghezza / 2 - radius, lunghezza / 2 - radius, radius * 2, radius * 2)); // <-
//			imp3.getRoi().setStrokeColor(Color.red);
//			over1.addElement(imp3.getRoi());
			// disegno
			// quadrato,
			// faccio
			// vertice
			// in alto a sinistra poi
			// inciccionisco
		} else {
			IJ.log(ReadData.qui() + "DUE");
			// la roi viene posizionata come la precedente
			imp3.setRoi(new OvalRoi(roiData[0] - roiData[2], roiData[1] - roiData[2], roiData[2] * 2, roiData[2] * 2));
//			ReadData.waitHere("disegnata roi x= " + (roiData[0] - roiData[2]) + " y= " + (roiData[1] - roiData[2])
//					+ " diam= " + (roiData[2] * 2));
//			imp3.getRoi().setStrokeColor(Color.blue);
//			over1.addElement(imp3.getRoi());

		}
		// imp3.updateAndDraw();

		if (descrizionePopUp != null) {
			new WaitForUserDialog(descrizionePopUp).show();
		}
		// =========================================================================
		// ===MODIFICA_02_11_21=====================================================
		// =========================================================================
		// vado a leggere dove hanno posizionato il cerchio, sono costretto ad usare
		// double per metterli nei risultati

		Roi roi3 = imp3.getRoi();
		if (roi3 == null)
			ReadData.waitHere("roi3==null");
		Rectangle boundingRectangle = roi3.getBounds();
//		IJ.log("" + roi3);
//
//		ImageProcessor ip3 = imp3.getProcessor();
//		Rectangle boundingRectangle = ip3.getRoi();

//		Rectangle boundingRectangle = imp3.getProcessor().getRoi();
		double xCenterRoi3 = Math.round(boundingRectangle.x + boundingRectangle.width / 2);
		double yCenterRoi3 = Math.round(boundingRectangle.y + boundingRectangle.height / 2);
		double radius3 = Math.round(boundingRectangle.width / 2);
		// ReadData.waitHere("xcenter= " + xCenterRoi3 + " yCenter= " + yCenterRoi3 + "
		// radius= " + radius3);
		// ========================================================
		// =========================================================================

		// OvalRoi oval1 = (OvalRoi) imp3.getRoi(); // <- leggo linea che è stata mossa
		// da operatore

		// per avere statistiche
		ImageStatistics stat1 = imp3.getStatistics();
		double meanStat1 = stat1.mean;
		IJ.log("Media: " + meanStat1);
		double devSt1 = stat1.stdDev;
		IJ.log("Dev st: " + devSt1);
		imp3.updateAndDraw();
		imp3.getRoi().setStrokeColor(Color.green);
		over1.addElement(imp3.getRoi());
		imp3.updateAndDraw();
		imp3.killRoi();

		double[] resultROI = { meanStat1, devSt1, xCenterRoi3, yCenterRoi3, radius3 };

		return resultROI;
	}

	public static void analyzeTagRiproducibilita(String pathReport, String lettera, String numero) {
		String pathFolderImm1 = ReadData.openWindowImage("riproducibilita' " + lettera + " " + numero);
		if (pathFolderImm1 == null) {
			ReadData.appendLog(pathReport, "POTA, premuto annulla");
			return;
		}
		ImagePlus imp1 = ReadData.imageOpenAndShow(pathFolderImm1);
		ReadData.analyzeALLTagDicom(imp1, pathReport, lettera, numero);
		imp1.changes = false;
		imp1.close();
		return;
	}

	public static void analyzeTagRiproducibilita(String pathReport, String pathFolderImm1, String lettera,
			String numero) {
		ImagePlus imp1 = ReadData.imageOpenAndShow(pathFolderImm1);
		ReadData.analyzeALLTagDicom(imp1, pathReport, lettera, numero);
		imp1.changes = false;
		imp1.close();
		return;
	}

	public static long[] selectFlatPanel() {
		ArrayList<String> tabella = new ArrayList<String>();
		InputStream in;
		try {
			// cerco il mio file e lo metto come InputStream
			in = ReadData.class.getResourceAsStream("/FlatConfig.csv");
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			while (br.ready()) {
				// leggo il BufferedReader superstringa dopo superstringa fino alla sua fine
				String line = br.readLine();
				// scrivo in tabella la superstringa
				tabella.add(line);
			}
			// chiudo
			br.close();
		} catch (Exception e) {
			return null;
		}
		// trasformiamo l'ArrayList in un normale array di stringhe
		String[] tabellaBella = tabella.toArray(new String[tabella.size()]);
		// la tabella brutta omette la prima riga del file, contenente le intestazioni
		String[] tabellaBrutta = new String[tabellaBella.length - 1];
		int conta = 0;
		for (int i1 = 1; i1 < tabellaBella.length; i1++) {
			tabellaBrutta[conta++] = tabellaBella[i1];
		}
		String[] vetMacchina = new String[tabellaBrutta.length];
		String[] vetMatricolaTubo = new String[tabellaBrutta.length];
		String[] vetRiserva = new String[tabellaBrutta.length];
		String[] vetApertura = new String[tabellaBrutta.length];
		String[] vetWidth = new String[tabellaBrutta.length];
		String[] vetHeight = new String[tabellaBrutta.length];
		String[] vetOffset = new String[tabellaBrutta.length];
		String[] vetLittleEndian = new String[tabellaBrutta.length];
		String[] vetDimPixel = new String[tabellaBrutta.length];

		for (int i1 = 0; i1 < tabellaBrutta.length; i1++) {
			String aux1 = tabellaBrutta[i1];
			String[] vetString = aux1.split(";");
			vetMacchina[i1] = vetString[0].trim();
			vetMatricolaTubo[i1] = vetString[1].trim();
			vetRiserva[i1] = vetString[2].trim();
			vetApertura[i1] = vetString[3].trim();
			vetWidth[i1] = vetString[4].trim();
			vetHeight[i1] = vetString[5].trim();
			vetOffset[i1] = vetString[6].trim();
			vetLittleEndian[i1] = vetString[7].trim();
			vetDimPixel[i1] = vetString[8].trim();
		}

		// ========= DIALOGO PER SELEZIONE MACCHINA ==============================
		GenericDialog gd = new GenericDialog("SELEZIONE MACCHINA");
		gd.addChoice("Selezione Macchina:", vetMacchina, "");
		gd.showDialog();
		if (gd.wasCanceled())
			return null;
		int scelta = gd.getNextChoiceIndex();

		long[] riassunto = new long[4];
		riassunto[0] = Long.parseLong(vetWidth[scelta]);
		riassunto[1] = Long.parseLong(vetHeight[scelta]);
		riassunto[2] = Long.parseLong(vetOffset[scelta]);
		riassunto[3] = Long.parseLong(vetDimPixel[scelta]);
//		// riassunto[3] = Long.parseLong(vetLittleEndian[scelta]);
//
//		ReadData.waitHere("Width " + riassunto[0] + "Height " + riassunto[1] + "Offset " + riassunto[2] + "DimPixel "
//				+ riassunto[3]);
//
//		ReadData.waitHere("I dati che recuperiamo per la macchiona sono: \n" + "macchina= " + vetMacchina[scelta]
//				+ "\nwidth= " + vetWidth[scelta] + "\nheight= " + vetHeight[scelta] + "\noffset= " + vetOffset[scelta]);

		return riassunto;

	}

	public static ImagePlus openRaw(String path, int width, int height, long offset) {

		FileInfo fi = new FileInfo();
//		fi.fileFormat = fi.RAW;
		fi.fileType = FileInfo.GRAY16_UNSIGNED;
		fi.width = width;
		fi.height = height;
		fi.longOffset = offset;
		fi.intelByteOrder = true;
		ImagePlus imp1 = Raw.open(path, fi);
		new ContrastEnhancer().stretchHistogram(imp1.getProcessor(), 0.5); // <- per migliorare il contrasto
		imp1.show();
		ReadData.zoom(imp1);
		
		return imp1;
	}

	public static String chooseImageRaw() {
		String oldPathReport = Prefs.get("FlatPanel.pathRaw", ""); // <- uso "" per dire che voglio prendere
		// quell'indirizzo della cartella
		OpenDialog.setDefaultDirectory(oldPathReport);
		OpenDialog od = new OpenDialog("Seleziona immagine desiderata", null);
		String folder = od.getDirectory();
		String file = od.getFileName();
		String path = folder + file;
		// ReadData.waitHere(path);
		Prefs.set("FlatPanel.pathRaw", folder); // <- mette il nuovo percorso che ho aperto prima dentro il
		// file pref.txt
		return path;
	}

	public static double[] ROI6cm(ImagePlus imp1, float dimPixel, int width, int height) {
		// devo fare ROI quadrata di 6cm -> converto 6cm in pixel
		float dimPixelF = (float) dimPixel;
		int lato = (int) (6 / (dimPixelF / 10000));
		// ReadData.waitHere("lato in pixel: " + lato);

		Overlay over1 = new Overlay();
		imp1.setOverlay(over1);
		imp1.killRoi();
		imp1.setRoi(new Roi((width / 2) - (lato / 2), (height / 2) - (lato / 2), lato, lato));
		ImageStatistics stat1 = imp1.getStatistics();
		double meanStat1 = stat1.mean;
		IJ.log("ROI6cm Media: " + meanStat1);
		double devSt1 = stat1.stdDev;
		IJ.log("ROI6cm Dev st: " + devSt1);

		imp1.getRoi().setStrokeColor(Color.blue);
		over1.addElement(imp1.getRoi());
		imp1.updateAndDraw();
		imp1.killRoi();
		// come si fa poi a rimuovere anche overly per togliere quadrato verde?

		double[] resultROI = { meanStat1, devSt1 };
		return resultROI;
	}

	public static void convertImageToDose(ImagePlus imp7, double m, double q) {
		ImageConverter ic7 = new ImageConverter(imp7);
		ic7.convertToGray32();
		ImageProcessor ip7 = imp7.getProcessor();
		ip7.convertToFloat();
		ip7.multiply(m);
		ip7.add(q);

		new ContrastEnhancer().stretchHistogram(imp7.getProcessor(), 0.5); // <- per migliorare il contrasto
		imp7.updateAndDraw();
	}

	public static float findMAXMatrix(float[][] matrix) {
		float MAX = 0;

		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				if (MAX < matrix[i][j]) {
					MAX = matrix[i][j];
				}
			}
		}
		return MAX;
	}
	
	public static float findMINMatrix(float[][] matrix) {
		float MIN = 100000;

		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				if (MIN > matrix[i][j]) {
					MIN = matrix[i][j];
				}
			}
		}
		return MIN;
	}

}
