/*
 * Skeleton class for the Lucene search program implementation
 * Assignment solution on 2016-04-11
 * * Antti Tolppanen 289795 <antti.tolppanen@aalto.fi>
 * * Fredrik Kaksonen 220848 <fredrik.kaksonen@aalto.fi>
 * * Annukka Jänkälä 220806
 * * Joanna Mehtälä 345480
 * 
 */
package ir_course;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

public class LuceneSearchApp {

	public LuceneSearchApp() {
	}

	EnglishAnalyzer engAnalyzer;
	StandardAnalyzer standAnalyzer = new StandardAnalyzer();
	Directory index;
	IndexWriterConfig config;
	
	
	public static void addDoc(IndexWriter w, DocumentInCollection feedItem)
			throws IOException {
		Document doc = new Document();
		doc.add(new TextField("title", feedItem.getTitle(), Field.Store.YES));
		doc.add(new TextField("abstract", feedItem.getAbstractText(),Field.Store.YES));
		doc.add(new IntField("taskNumber", feedItem.getSearchTaskNumber(),Field.Store.YES));
		doc.add(new TextField("query", feedItem.getQuery(), Field.Store.YES));
		doc.add(new TextField("relevance", feedItem.isRelevant() ? "1" : "0",Field.Store.YES));
		w.addDocument(doc);
	}

	// Method for indexing using EnglishAnalyzer (Porter stemming + default stop word set)
	public void index(List<DocumentInCollection> docs) throws IOException {
		//EnglishAnalyzer with default stop word set
		engAnalyzer = new EnglishAnalyzer();
		index = new RAMDirectory();
		config = new IndexWriterConfig(engAnalyzer);
		IndexWriter writer = new IndexWriter(index, config);
		for (int i = 0; i < docs.size(); i++) {
			if (docs.get(i).getSearchTaskNumber() == 3)
				addDoc(writer, docs.get(i));
		}
		writer.close();
	}
	
	// Method for indexing using StandardAnalyzer (no stemming + default stop word set)
	public void index2(List<DocumentInCollection> docs) throws IOException {
		index = new RAMDirectory();
		config = new IndexWriterConfig(standAnalyzer);
		IndexWriter writer = new IndexWriter(index, config);
		for (int i = 0; i < docs.size(); i++) {
			if (docs.get(i).getSearchTaskNumber() == 3)
				addDoc(writer, docs.get(i));
		}
		writer.close();
	}
	
	// Method for indexing using modified EnglishAnalyzer (Porter stemming + NO stop words)
	public void index3(List<DocumentInCollection> docs) throws IOException {	
		//EnglishAnalyzer with NO stop words
		CharArraySet stopwords = new CharArraySet(1, false);
		stopwords.clear();
		engAnalyzer = new EnglishAnalyzer(stopwords);
			
		index = new RAMDirectory();
		config = new IndexWriterConfig(engAnalyzer);
		IndexWriter writer = new IndexWriter(index, config);
		for (int i = 0; i < docs.size(); i++) {
			if (docs.get(i).getSearchTaskNumber() == 3)
				addDoc(writer, docs.get(i));
		}
		writer.close();
	}
	
	// Search method - read comments inside the method
	public List<String> search(String queryString)
			throws IOException, ParseException {		
			DirectoryReader reader = DirectoryReader.open(index);
			IndexSearcher searcher = new IndexSearcher(reader);
			
			//To change ranking method from VSM to BM25 uncomment line below
			//searcher.setSimilarity(new BM25Similarity());
			
			Query q = new QueryParser("abstract", standAnalyzer).parse(queryString);
			ScoreDoc[] hits = searcher.search(q,null,100).scoreDocs;
			
			List<String> results = new LinkedList<String>();
			for (int i = 0; i < hits.length; i++) {
				results.add("¨" + hits[i].score + "¨" + searcher.doc(hits[i].doc).get("title") + "¨"
						+ searcher.doc(hits[i].doc).get("abstract") + "¨"
						+ searcher.doc(hits[i].doc).get("relevance") + "¨"
						+ searcher.doc(hits[i].doc).get("query"));
			}
			return results;
	}

	// Method for printing the search results
	public void printResults(List<String> results) {
		if (results.size() > 0) {
			for (int i = 0; i < results.size(); i++) {
				System.out.println((i+1) + results.get(i));
			}
		} else
			System.out.println(" no results");
	}
	
	// Main method - read comments inside the method
	public static void main(String[] args) throws IOException, ParseException {
		if (args.length > 0) {
			LuceneSearchApp engine = new LuceneSearchApp();
			DocumentCollectionParser parser = new DocumentCollectionParser();
			parser.parse(args[0]);
			List<DocumentInCollection> docs = parser.getDocuments();
			List<String> results = new LinkedList<String>();
			
			//The indexing method used, to change indexing method: comment and uncomment lines below
				// Indexing with Porter stemming and stop words
			engine.index(docs);
				// Indexing with no stemming but with stop words
			//engine.index2(docs);
				// Indexing with Porter stemming and NO stop words
			//engine.index3(docs);
			
			//The search query used, to change query: comment and uncomment lines below
			results = engine.search("Content based video annotation");
			//results = engine.search("Automatic or semiautomatic video tagging");
			//results = engine.search("feature based Multimedia annotation");
			
			engine.printResults(results);
			
		} else
			System.out
					.println("ERROR: the path of a RSS Feed file has to be passed as a command line argument.");
	}
}
