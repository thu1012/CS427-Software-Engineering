package edu.illinois.cs.debugging;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import jdk.incubator.vector.VectorOperators;

/**
 * Implementation for Spectrum-based Fault Localization (SBFL).
 *
 */
public class SBFL
{

    /**
    * Use Jsoup to parse the coverage file in the XML format.
    * 
    * @param file
    * @return a map from each test to the set of lines that it covers
    * @throws FileNotFoundException
    * @throws IOException
    */
    protected static Map<String, Set<String>> readXMLCov(File file) throws FileNotFoundException, IOException {
        FileInputStream fileInputStream = new FileInputStream(file);

        Map<String, Set<String>> res = new HashMap<String, Set<String>>();
        Document document = Jsoup.parse(fileInputStream, "UTF-8", "",
            Parser.xmlParser());

        Elements tests = document.select("test");
        for (Element test : tests) {
            Element name = test.child(0);
            Element covLines = test.child(1);

            Set<String> lines = new HashSet<String>();
            String[] items = covLines.text().split(", ");
            Collections.addAll(lines, items);
            res.put(name.text(), lines);
        }
        return res;
    }

    /**
    * Compute the suspiciousness values for all covered statements based on
    * Ochiai
    * 
    * @param cov
    * @param failedTests
    * @return a map from each line to its suspiciousness value
    */
    public static Map<String, Double> Ochiai(Map<String, Set<String>> cov,
        Set<String> failedTests) {
        // using LinkedHashMap so that the statement list can be ranked
        Map<String, Double> susp = new LinkedHashMap<String, Double>();
        // TODO
        Map<String, Integer> fail = new HashMap<>();
        Map<String, Integer> pass = new HashMap<>();

        for (Map.Entry<String, Set<String>> entry : cov.entrySet()) {
            String name = entry.getKey();
            Set<String> covLines = entry.getValue();

            for(String line : covLines) {
                if (failedTests.contains(name)) {
                    fail.put(line, fail.getOrDefault(line, 0) + 1);
                } else {
                    pass.put(line, pass.getOrDefault(line, 0) + 1);
                }
            }
        }

        for(String line : fail.keySet()) {
            int fails = fail.getOrDefault(line, 0);
            int passes = pass.getOrDefault(line, 0);
            if (failedTests.size() * (fails+passes) > 0) {
                double score = fails/Math.sqrt(failedTests.size() * (fails + passes));
                susp.put(line, score);
            } else {
                susp.put(line, 0.0);
            }
        }

        return susp;
    }

    /**
    * Get the suspiciousness value for the buggy line from the suspicious
    * statement list
    * 
    * @param susp
    * @param buggyLine
    * @return the suspiciousness value for the buggy line
    */
    protected static double getSusp(Map<String, Double> susp,
        String buggyLine) {
        // TODO
        return susp.getOrDefault(buggyLine, 0.0);
    }

    /**
    * Rank all statements based on the descending order of their suspiciousness
    * values. Get the rank (print the lowest rank for the tied cases) for the
    * buggy line from the suspicious statement list
    * 
    * @param susp
    * @param buggyLine
    * @return the rank of the buggy line
    */
    protected static int getRank(Map<String, Double> susp, String buggyLine) {
        List<Map.Entry<String, Double>> list = new LinkedList<Map.Entry<String, Double>>(
            susp.entrySet());
        // TODO
        if (!susp.containsKey(buggyLine)) {
            return 0;
        }

        double buggyScore = susp.get(buggyLine);
        int rank = 0;
        for (double score : susp.values()) {
            if (Double.compare(score, buggyScore) >= 0) {
                rank++;
            }
        }

        return rank;
    }
}
