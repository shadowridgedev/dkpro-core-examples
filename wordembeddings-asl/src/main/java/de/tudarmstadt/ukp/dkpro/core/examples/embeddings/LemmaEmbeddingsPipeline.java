/*******************************************************************************
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.examples.embeddings;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.mallet.wordembeddings.MalletEmbeddingsTrainer;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordPosTagger;
import de.tudarmstadt.ukp.dkpro.core.stopwordremover.StopWordRemover;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.SimplePipeline;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;

/**
 * A pipeline that estimates word embeddings based on the lemmas, annotated in a previous step.
 * Here, lemmatization is done using the {@link StanfordLemmatizer}.
 * The {@link #FEATURE_PATH} provides the reference to the annotation that is used by the embeddings trainer ({@link MalletEmbeddingsTrainer}).
 */
public class LemmaEmbeddingsPipeline
{
    public static final File TARGET_FILE = new File("target/lemma_embeddings.txt");
    private static final String LANGUAGE = "en";
    private static final URL STOPWORD_FILE = EmbeddingsPipeline.class.getClassLoader()
            .getResource("stopwords_en.txt");
    private static final String DEFAULT_SOURCE_DIR = "src/main/resources/texts/*";
    private static final int NUM_THREADS = 1;   // do not use multiple threads for very small (test) datasets or the estimator may run infinitely!
    /**
     * Defines the feature path to the lemmas that are used by the embeddings trained.
     */
    private static final String FEATURE_PATH = Token.class.getTypeName() + "/lemma/value";

    public static void main(String[] args)
            throws IOException, UIMAException
    {
        String inputDir = args.length > 0 ? args[0] : DEFAULT_SOURCE_DIR;

        CollectionReaderDescription reader = createReaderDescription(TextReader.class,
                TextReader.PARAM_SOURCE_LOCATION, inputDir,
                TextReader.PARAM_LANGUAGE, LANGUAGE);
        AnalysisEngineDescription segmenter = createEngineDescription(OpenNlpSegmenter.class);
        AnalysisEngineDescription posTagger = createEngineDescription(StanfordPosTagger.class);
        AnalysisEngineDescription stopwordRemover = createEngineDescription(StopWordRemover.class,
                StopWordRemover.PARAM_MODEL_LOCATION, STOPWORD_FILE);
        AnalysisEngineDescription lemmatizer = createEngineDescription(StanfordLemmatizer.class);
        AnalysisEngineDescription embeddings = createEngineDescription(
                MalletEmbeddingsTrainer.class,
                MalletEmbeddingsTrainer.PARAM_TARGET_LOCATION, TARGET_FILE,
                MalletEmbeddingsTrainer.PARAM_NUM_THREADS, NUM_THREADS,
                MalletEmbeddingsTrainer.PARAM_TOKEN_FEATURE_PATH, FEATURE_PATH);

        SimplePipeline
                .runPipeline(reader, segmenter, posTagger, lemmatizer, stopwordRemover, embeddings);
    }
}
