/*
 * Skeleton class for the Lucene search program implementation
 * Assignment solution on 2016-04-11
 * * Antti Tolppanen 289795 <antti.tolppanen@aalto.fi>
 * 
 * 
 */
package ir_course;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

public class LuceneSearchApp {

	public LuceneSearchApp() {

	}
	


	EnglishAnalyzer analyzer;
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

	public void index(List<DocumentInCollection> docs) throws IOException {
		analyzer = new EnglishAnalyzer();
		index = new RAMDirectory();
		config = new IndexWriterConfig(analyzer);
		IndexWriter writer = new IndexWriter(index, config);
		for (int i = 0; i < docs.size(); i++) {
			if (docs.get(i).getSearchTaskNumber() == 3)
				addDoc(writer, docs.get(i));
		}
		writer.close();
	}
	


	public List<String> search(String queryString)
			throws IOException, ParseException {		
			DirectoryReader reader = DirectoryReader.open(index);
			IndexSearcher searcher = new IndexSearcher(reader);
			Query q = new QueryParser("abstract", analyzer).parse(queryString);
			ScoreDoc[] hits = searcher.search(q,null,100).scoreDocs;
			
			
			
			List<String> results = new LinkedList<String>();

			for (int i = 0; i < hits.length; i++) {
				Document result = searcher.doc(hits[i].doc);
				results.add("Score:" + " "  + hits[i].score + " - " + result.toString());
			}

			return results;

	}

	public void printQuery(Query searchQuery) {
		System.out.println("Search (");
		if (searchQuery != null) {
			System.out.println("in document: " + searchQuery);
			if (searchQuery != null)
				System.out.println("; ");
		}

	}

	public void printResults(List<String> results) {
		if (results.size() > 0) {
			for (int i = 0; i < results.size(); i++) {
				System.out.println(" " + (i + 1) + ". " + results.get(i));
				System.out.println("=========================================");
			}
		} else
			System.out.println(" no results");
	}
	


	public static void main(String[] args) throws IOException, ParseException {
		if (args.length > 0) {
			LuceneSearchApp engine = new LuceneSearchApp();

			DocumentCollectionParser parser = new DocumentCollectionParser();
			parser.parse(args[0]);
			List<DocumentInCollection> docs = parser.getDocuments();
			List<String> results = new LinkedList<String>();

			engine.index(docs);
			
			

			//Input search string as an argument on the line below i.e. "video media"
			results = engine.search("video media");
			engine.printResults(results);

		} else
			System.out
					.println("ERROR: the path of a RSS Feed file has to be passed as a command line argument.");
	}
}
