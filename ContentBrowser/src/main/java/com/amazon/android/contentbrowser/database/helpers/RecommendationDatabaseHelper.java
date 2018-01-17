/**
 * Copyright 2015-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *     http://aws.amazon.com/apache2.0/
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.amazon.android.contentbrowser.database.helpers;

import com.amazon.android.contentbrowser.database.records.RecommendationRecord;
import com.amazon.android.contentbrowser.database.tables.RecommendationTable;
import com.amazon.utils.DateAndTimeHelper;
import com.amazon.utils.StringManipulation;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class that has all the functionality specific to the {@link RecommendationTable}.
 */
public class RecommendationDatabaseHelper extends DatabaseHelper {
    
    private static final String TAG = RecommendationDatabaseHelper.class.getSimpleName();
    
    /**
     * Static instance of recommendation database helper.
     */
    private static RecommendationDatabaseHelper sInstance;
    
    /**
     * Get the recommendation database helper instance.
     *
     * @return The helper instance.
     */
    public static RecommendationDatabaseHelper getInstance() {
        
        if (sInstance == null) {
            synchronized (RecommendationDatabaseHelper.class) {
                if (sInstance == null) {
                    sInstance = new RecommendationDatabaseHelper();
                }
            }
        }
        return sInstance;
    }
    
    /**
     * Constructor.
     */
    private RecommendationDatabaseHelper() {
        
        super(new RecommendationTable());
    }
    
    /**
     * Store or update a recommendation in the database. If an existing entry is found for
     * the given recommendation id, the record is updated with the new information.
     *
     * @param contentId        The content id.
     * @param recommendationId The recommendation id.
     * @param type             The recommendation type.
     * @return True if a record was entered or updated in the database; false otherwise.
     */
    public boolean addRecord(Context context, String contentId, int recommendationId, String type) {
        
        if (StringManipulation.isNullOrEmpty(contentId) || recommendationId <= 0 ||
                StringManipulation.isNullOrEmpty(type)) {
            Log.e(TAG, "Parameters cannot be null or 0 when saving a recommendation to " +
                    "database: contentId=" + contentId + ", recommendationId=" + recommendationId
                    + ", type=" + type);
            return false;
        }

        return writeRecord(context, new RecommendationRecord(contentId, recommendationId, type));
    }
    
    /**
     * Store or update a recommendation in the database. If an existing entry is found for
     * the given recommendation id, the record is updated with the new information.
     *
     * @param record The record to update.
     * @return True if a record was entered or updated in the database; false otherwise.
     */
    public boolean addRecord(Context context, RecommendationRecord record) {
        
        return addRecord(context, record.getContentId(), record.getRecommendationId(),
                         record.getType());
    }
    
    /**
     * Deletes the recommendation record for the given recommendation id.
     *
     * @param recommendationId The recommendation id.
     * @return True if the record was deleted; false otherwise.
     */
    public boolean deleteByRecId(Context context, long recommendationId) {
        
        return ((RecommendationTable) getTable()).deleteByRecommendationId(getDatabase(context),
                                                             recommendationId);
    }
    
    /**
     * Deletes all recommendation records with the given type.
     *
     * @param type The recommendation type.
     * @return True if at least one record was deleted; false otherwise.
     */
    public boolean deleteAllRecsWithType(Context context, String type) {
        
        if (StringManipulation.isNullOrEmpty(type)) {
            Log.e(TAG, "Type cannot be null or empty when deleting recommendations by type from " +
                    "database");
            return false;
        }
        
        return ((RecommendationTable) getTable()).deleteAllRecordsWithType(getDatabase(context),
                                                                           type);
    }
    
    /**
     * Get all recommendation records with the given type.
     *
     * @param type The recommendation type.
     * @return List of recommendation records.
     */
    public List<RecommendationRecord> getRecsWithType(Context context, String type) {
        
        if (StringManipulation.isNullOrEmpty(type)) {
            Log.e(TAG, "Type parameter cannot be null or empty when reading recommendations from " +
                    "database.");
            
            return null;
        }
        RecommendationTable table = (RecommendationTable) getTable();
        return (List<RecommendationRecord>)
                table.readMultipleRecords(getDatabase(context),
                                          table.getRecommendationTableSelectTypeQuery(type));
    }
    
    /**
     * Gets the recommendation record for the given recommendation id.
     *
     * @param recommendationId The recommendation id.
     * @return The recommendation record.
     */
    public RecommendationRecord getRecByRecId(Context context, long recommendationId) {
        
        if (recommendationId <= 0) {
            Log.e(TAG, "Recommendation id cannot be 0 or negative when reading a recommendation " +
                    "from database.");
            return null;
        }
        
        return ((RecommendationTable) getTable()).read(getDatabase(context), recommendationId);
    }
    
    /**
     * Update a recommendation record.
     *
     * @param record The record.
     * @return The row of the updated record or -1 if there was an error when updating.
     */
    public long updateRecord(Context context, RecommendationRecord record) {
        
        return getTable().write(getDatabase(context), record);
    }
    
    /**
     * Giving a list of content ids, return a list of records for any existing records with an id
     * from the list.
     *
     * @param contentIds List of content ids.
     * @return List of records containing the the same content ids.
     */
    public List<RecommendationRecord> getExistingRecommendationsByContentIds(Context context,
                                                                             List<String>
                                                                                     contentIds) {
        
        List<RecommendationRecord> matchingRecords = new ArrayList<>();
        for (String contentId : contentIds) {
            if (recordExists(context, contentId)) {
                matchingRecords.add(getRecord(context, contentId));
            }
        }
        return matchingRecords;
    }
    
    /**
     * Get the list of expired recommendation records from the database. Recommendations are
     * expired if their expiration date has been reached.
     *
     * @return The list of expired recommendations.
     */
    public List<RecommendationRecord> getExpiredRecommendations(Context context) {
        
        return ((RecommendationTable) getTable()).getExpiredRecommendations(getDatabase(context),
                                                              DateAndTimeHelper.getCurrentDate()
                                                                               .getTime());
    }
    
    /**
     * Get a list of recommendation ids from the database.
     *
     * @return A list of ids.
     */
    public List<Integer> getAllRecommendationsIds(Context context) {
        
        return ((RecommendationTable) getTable()).getRecommendationIds(getDatabase(context));
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public RecommendationRecord getRecord(Context context, String contentId) {
        
        return (RecommendationRecord) super.getRecord(context, contentId);
    }
}
