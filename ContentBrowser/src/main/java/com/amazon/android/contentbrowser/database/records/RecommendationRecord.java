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
package com.amazon.android.contentbrowser.database.records;

/**
 * This class represents a recommendation stored in the database.
 **/
public class RecommendationRecord extends Record {
    
    private static final String TAG = RecommendationRecord.class.getSimpleName();
    
    /**
     * A tag for the generic recommendation type.
     */
    public static final String GLOBAL = "Global";
    
    /**
     * A tag for the related recommendation type.
     */
    public static final String RELATED = "Related";
    
    /**
     * The recommendation id.
     */
    private int mRecommendationId;
    
    /**
     * The type of recommendation.
     */
    private String mType;
    
    /**
     * The recommendation record constructor.
     */
    public RecommendationRecord() {
        
    }
    
    /**
     * A recommendation record constructor.
     *
     * @param contentId        The content id.
     * @param recommendationId The recommendation id.
     * @param type             The type of recommendation.
     */
    public RecommendationRecord(String contentId, int recommendationId, String type) {
        
        super(contentId);
        mRecommendationId = recommendationId;
        mType = type;
    }
    
    /**
     * Get the recommendation id.
     *
     * @return The recommendation id.
     */
    public int getRecommendationId() {
        
        return mRecommendationId;
    }
    
    /**
     * Set the recommendation id.
     *
     * @param recommendationId The recommendation id.
     */
    public void setRecommendationId(int recommendationId) {
        
        mRecommendationId = recommendationId;
    }
    
    /**
     * Get the recommendation type.
     *
     * @return The type.
     */
    public String getType() {
        
        return mType;
    }
    
    /**
     * Set the recommendation type.
     *
     * @param type The type.
     */
    public void setType(String type) {
        
        mType = type;
    }
    
    @Override
    public boolean equals(Object object) {
        
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        
        RecommendationRecord record = (RecommendationRecord) object;
        
        return !(getContentId() == null || !getContentId().equals(record.getContentId())) &&
                getRecommendationId() == record.getRecommendationId() && !(getType() == null ||
                !getType().equals(record.getType()));
    }
    
    @Override
    public String toString() {
        
        return "RecommendationRecord{" +
                "mContentId='" + getContentId() + '\'' +
                ", mRecommendationId=" + mRecommendationId +
                ", mType='" + mType + '\'' +
                '}';
    }
}
