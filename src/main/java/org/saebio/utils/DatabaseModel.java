package org.saebio.utils;

import org.saebio.sample.Sample;

public interface DatabaseModel {
    enum InsertStatus {
        SAMPLE_INSERT_ERROR,
        SAMPLE_INSERTED_SUCCESSFULLY,
        SAMPLE_ALREADY_EXISTS,
    }

    boolean testConnection();

    InsertStatus addSample(Sample sample);

    Sample getFirstSampleFromCurrentEpisode(String NHC);

    int getRowCount();

    boolean updateSampleReasonLineageAndVariant(Sample sample);
}
