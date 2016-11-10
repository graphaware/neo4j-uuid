/*
 * Copyright (c) 2013-2016 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
package com.graphaware.module.guid;

import com.graphaware.common.policy.InclusionPolicies;
import com.graphaware.common.policy.none.IncludeNoRelationships;
import com.graphaware.runtime.config.BaseTxDrivenModuleConfiguration;
import com.graphaware.runtime.policy.InclusionPoliciesFactory;


/**
 * {@link BaseTxDrivenModuleConfiguration} for {@link com.graphaware.module.guid.GuidModule}.
 */
public class GuidConfiguration extends BaseTxDrivenModuleConfiguration<GuidConfiguration> {

	private static final String DEFAULT_GUID_GENERATOR = "com.graphaware.module.guid.generator.EaioUUIDGenerator";
	
    private static final String DEFAULT_GUID_PROPERTY = Properties.GUID;
    private static final String DEFAULT_GUID_NODEX_INDEX = Indexes.GUID_NODE_INDEX;
    private static final String DEFAULT_GUID_REL_INDEX = Indexes.GUID_REL_INDEX;
    private static final boolean DEFAULT_STRIP_HYPHENS = false;

    private final String GuidGenerator;
    private final String guidProperty;
    private final String guidIndex;
    private final String guidRelationshipIndex;
    private final Boolean stripHyphens;

    private GuidConfiguration(InclusionPolicies inclusionPolicies, long initializeUntil, String GuidGenerator, String guidProperty, boolean stripHyphens, String guidIndex, String guidRelationshipIndex) {
        super(inclusionPolicies, initializeUntil);
        this.GuidGenerator = GuidGenerator;
        this.guidProperty = guidProperty;
        this.guidIndex = guidIndex;
        this.guidRelationshipIndex = guidRelationshipIndex;
        this.stripHyphens = stripHyphens;
    }

    /**
     * Create a default configuration with default guid property = {@link #DEFAULT_GUID_PROPERTY},
     * guid index = {@link #DEFAULT_GUID_NODEX_INDEX}
     * inclusion policies = {@link InclusionPoliciesFactory#allBusiness()} with {@link IncludeNoRelationships},
     * and initialize until = {@link #ALWAYS}.
     * <p/>
     * Change this by calling {@link #withGuidProperty(String)}, with* other inclusion strategies
     * on the object, always using the returned object (this is a fluent interface).
     */
    public static GuidConfiguration defaultConfiguration() {
        return new GuidConfiguration(InclusionPoliciesFactory
                .allBusiness()
                .with(IncludeNoRelationships.getInstance())
                , ALWAYS, DEFAULT_GUID_GENERATOR, DEFAULT_GUID_PROPERTY, DEFAULT_STRIP_HYPHENS, DEFAULT_GUID_NODEX_INDEX, DEFAULT_GUID_REL_INDEX);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected GuidConfiguration newInstance(InclusionPolicies inclusionPolicies, long initializeUntil) {
        return new GuidConfiguration(inclusionPolicies, initializeUntil, getGuidGenerator(), getGuidProperty(), shouldStripHyphens(), getGuidIndex(), getGuidRelationshipIndex());
    }

    public String getGuidGenerator() {
		return GuidGenerator;
	}

	public String getGuidProperty() {
        return guidProperty;
    }

    public String getGuidIndex() {
        return guidIndex;
    }

    public String getGuidRelationshipIndex() {
        return guidRelationshipIndex;
    }

    public boolean shouldStripHyphens() {
        return stripHyphens;
    }

    /**
     * Create a new instance of this {@link GuidConfiguration} with different guid generator.
     *
     * @param guidProperty of the new instance.
     * @return new instance.
     */
    public GuidConfiguration withGuidGenerator(String GuidGenerator) {
    	return new GuidConfiguration(getInclusionPolicies(), initializeUntil(), GuidGenerator, getGuidProperty(), shouldStripHyphens(), getGuidIndex(), getGuidRelationshipIndex());
    }
    
    /**
     * Create a new instance of this {@link GuidConfiguration} with different guid property.
     *
     * @param guidProperty of the new instance.
     * @return new instance.
     */
    public GuidConfiguration withGuidProperty(String guidProperty) {
        return new GuidConfiguration(getInclusionPolicies(), initializeUntil(), getGuidGenerator(), guidProperty, shouldStripHyphens(), getGuidIndex(), getGuidRelationshipIndex());
    }

    /**
     * Create a new instance of this {@link GuidConfiguration} with different guid index.
     *
     * @param guidIndex of the new instance.
     * @return new instance.
     */
    public GuidConfiguration withGuidIndex(String guidIndex) {
        return new GuidConfiguration(getInclusionPolicies(), initializeUntil(), getGuidGenerator(), getGuidProperty(), shouldStripHyphens(), guidIndex, getGuidRelationshipIndex());
    }

    /**
     * Create a new instance of this {@link GuidConfiguration} with different guid relationship index.
     *
     * @param guidRelationshipIndex of the new instance.
     * @return new instance.
     */
    public GuidConfiguration withGuidRelationshipIndex(String guidRelationshipIndex) {
        return new GuidConfiguration(getInclusionPolicies(), initializeUntil(), getGuidGenerator(), getGuidProperty(), shouldStripHyphens(), getGuidIndex(), guidRelationshipIndex);
    }

    /**
     * Create a new instance of this {@link GuidConfiguration} with different guid index.
     *
     * @param stripHyphens property of new instance.
     * @return new instance.
     */
    public GuidConfiguration withStripHyphensProperty(boolean stripHyphens) {
        return new GuidConfiguration(getInclusionPolicies(), initializeUntil(), getGuidGenerator(), getGuidProperty(), stripHyphens, getGuidIndex(), getGuidRelationshipIndex());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        GuidConfiguration that = (GuidConfiguration) o;

        if (!guidProperty.equals(that.guidProperty)) {
            return false;
        }
        if (!guidIndex.equals(that.guidIndex)) {
            return false;
        }

        if (guidRelationshipIndex.equals(that.guidRelationshipIndex)) {
            return false;
        }

        if (stripHyphens != that.stripHyphens) {
            return false;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + GuidGenerator.hashCode();
        result = 31 * result + guidProperty.hashCode();
        result = 31 * result + guidIndex.hashCode();
        result = 31 * result + guidRelationshipIndex.hashCode();
        result = 31 * result + stripHyphens.hashCode();
        return result;
    }
}
