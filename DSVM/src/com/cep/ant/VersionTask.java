package com.cep.ant;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.GregorianCalendar;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class VersionTask extends Task {
	private static final String fileName = "./src/com/cep/darkstar/node/configuration/version/DarkStarVersion.java";
	private String version = null;
	
	public void execute() throws BuildException {
		new File(fileName).delete();
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
			writer.write("package com.cep.darkstar.node.configuration.version;\n");
			writer.write("public class DarkStarVersion {\n");
			writer.write("\tprivate final static String version=\"" + version + "\";\n");
			writer.write("\tprivate final static long buildTime=" + new GregorianCalendar().getTimeInMillis() + "L;\n");
			writer.write("\tpublic String getVersion() {return version;}\n");
			writer.write("\tpublic long getBuildTime() {return buildTime;}\n}");
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setVersion(String version) {
		this.version = version;
	}

}
