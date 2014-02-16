package com.cep.darkstar.lex;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.Gate;
import gate.creole.SerialAnalyserController;
import gate.util.persistence.PersistenceManager;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class GateAnalyzer {
	SerialAnalyserController controller;

	public GateAnalyzer(String appPath) throws Exception {
		Gate.init();
		controller = (SerialAnalyserController) PersistenceManager.loadObjectFromFile(new File(appPath));
	}
	
	@SuppressWarnings("unchecked")
	public Set<String> processDoc(String str) throws Exception {
		Set<String> toReturn = new HashSet<String>();
		Corpus c = null;
		Document aDoc = null;
		try {
			c = Factory.newCorpus("sample");
			aDoc = Factory.newDocument(str);
			c.add(aDoc);
			controller.setCorpus(c);
			controller.execute();
			AnnotationSet aSet = aDoc.getAnnotations("StockSymbols");
			for (Annotation annot : aSet) {
				String symbol = (String) annot.getFeatures().get("sym");
				toReturn.add(symbol);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (aDoc != null) {
				Factory.deleteResource(aDoc);
			}
			if (c != null) {
				Factory.deleteResource(c);
			}
		}
		return toReturn;
	}
	
}
