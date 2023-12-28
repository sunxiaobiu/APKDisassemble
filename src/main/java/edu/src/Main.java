package edu.src;

import edu.model.*;
import edu.model.sourcefile.ResourceLeakRule;
import edu.model.sourcefile.TypeStateRule;
import edu.util.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.xmlpull.v1.XmlPullParserException;
import soot.*;
import soot.dexpler.DalvikThrowAnalysis;
import soot.jimple.infoflow.android.manifest.ProcessManifest;
import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.ExceptionalUnitGraphFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {

    public static void main(String[] args) throws XmlPullParserException, IOException {
        String apkPath = args[0];
        String androidJarPath = args[1];

        long startTime = System.currentTimeMillis();
        System.out.println("==>START TIME:" + startTime);

        //calculate EntryPoint to generate dummyMainMethod
        EntryPointHelper entryPointHelper = calculateEntryPoint(apkPath, androidJarPath);

        JimpleBasedInterproceduralCFG baseICFG = new JimpleBasedInterproceduralCFG(true, true) {
            protected DirectedGraph<Unit> makeGraph(Body body) {
                return enableExceptions ? ExceptionalUnitGraphFactory.createExceptionalUnitGraph(body, DalvikThrowAnalysis.interproc(), true)
                        : new BriefUnitGraph(body);
            }
        };

        for (SootClass sootClass : Scene.v().getApplicationClasses()) {
            if (ApplicationClassFilter.isClassInSystemPackage(sootClass.getName())) {
                continue;
            }
            //todo:yitong retrieve and printout callgraph here..
        }

        long afterEntryPoint = System.currentTimeMillis();
        System.out.println("==>after EntryPoint TIME:" + afterEntryPoint);
    }

    private static void getMinTargetSDKVersion(String apkPath) throws IOException, XmlPullParserException {
        try {
            ProcessManifest manifest = new ProcessManifest(apkPath);
            GlobalRef.minSDKVersion = manifest.getMinSdkVersion();
            GlobalRef.targetSDKVersion = manifest.getTargetSdkVersion();
            System.out.println("====APK minSDKVersion====" + GlobalRef.minSDKVersion);
            System.out.println("====APK targetSDKVersion====" + GlobalRef.targetSDKVersion);
        } catch (Exception e) {
            GlobalRef.minSDKVersion = 30;
        }
    }

    public static EntryPointHelper calculateEntryPoint(String apkPath, String androidJarPath) throws XmlPullParserException, IOException {
        EntryPointHelper entryPointHelper = new EntryPointHelper();
        entryPointHelper.calculateEntryPoint(apkPath, androidJarPath);
        return entryPointHelper;
    }

}
