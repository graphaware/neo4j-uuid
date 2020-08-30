/*
 * Copyright (c) 2013-2020 GraphAware
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
package com.graphaware.module.uuid;

import com.graphaware.common.policy.inclusion.InclusionPolicies;
import com.graphaware.common.policy.inclusion.none.IncludeNoRelationships;
import com.graphaware.runtime.config.BaseModuleConfiguration;
import com.graphaware.runtime.config.ModuleConfiguration;
import com.graphaware.runtime.policy.InclusionPoliciesFactory;


/**
 * {@link ModuleConfiguration} for {@link com.graphaware.module.uuid.UuidModule}.
 */
public class UuidConfiguration extends BaseModuleConfiguration<UuidConfiguration> {

	private static final String DEFAULT_UUID_GENERATOR = "com.graphaware.common.uuid.EaioUuidGenerator";
	
    private static final String DEFAULT_UUID_PROPERTY = Properties.UUID;
    private static final String DEFAULT_UUID_NODEX_INDEX = Indexes.UUID_NODE_INDEX;
    private static final String DEFAULT_UUID_REL_INDEX = Indexes.UUID_REL_INDEX;
    private static final boolean DEFAULT_STRIP_HYPHENS = false;
    private static final boolean DEFAULT_IMMUTABLE = true;

    private final String uuidGenerator;
    private final String uuidProperty;
    private final String uuidIndex;
    private final String uuidRelationshipIndex;
    private final boolean stripHyphens;
    private final boolean immutable;


    private UuidConfiguration(InclusionPolicies inclusionPolicies, String uuidGenerator, String uuidProperty, boolean stripHyphens, String uuidIndex, String uuidRelationshipIndex, boolean immutable) {
        super(inclusionPolicies);
        this.uuidGenerator = uuidGenerator;
        this.uuidProperty = uuidProperty;
        this.uuidIndex = uuidIndex;
        this.uuidRelationshipIndex = uuidRelationshipIndex;
        this.stripHyphens = stripHyphens;
        this.immutable = immutable;
    }

    /**
     * Create a default configuration with default uuid property = {@link #DEFAULT_UUID_PROPERTY},
     * uuid index = {@link #DEFAULT_UUID_NODEX_INDEX}
     * and inclusion policies = {@link InclusionPoliciesFactory#allBusiness()} with {@link com.graphaware.common.policy.inclusion.none.IncludeNoRelationships}.
     * <p/>
     * Change this by calling {@link #withUuidProperty(String)}, with* other inclusion strategies
     * on the object, always using the returned object (this is a fluent interface).
     */
    public static UuidConfiguration defaultConfiguration() {
        return new UuidConfiguration(InclusionPoliciesFactory
                .allBusiness()
                .with(IncludeNoRelationships.getInstance())
                , DEFAULT_UUID_GENERATOR, DEFAULT_UUID_PROPERTY, DEFAULT_STRIP_HYPHENS, DEFAULT_UUID_NODEX_INDEX, DEFAULT_UUID_REL_INDEX, DEFAULT_IMMUTABLE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected UuidConfiguration newInstance(InclusionPolicies inclusionPolicies) {
        return new UuidConfiguration(inclusionPolicies, getUuidGenerator(), getUuidProperty(), shouldStripHyphens(), getUuidIndex(), getUuidRelationshipIndex(), getImmutable());
    }

    public String getUuidGenerator() {
		return uuidGenerator;
	}

	public String getUuidProperty() {
        return uuidProperty;
    }

    public String getUuidIndex() {
        return uuidIndex;
    }

    public String getUuidRelationshipIndex() {
        return uuidRelationshipIndex;
    }

    public boolean shouldStripHyphens() {
        return stripHyphens;
    }

    public boolean getImmutable() {
        return immutable;
    }

    /**
     * Create a new instance of this {@link UuidConfiguration} with different uuid generator.
     *
     * @param uuidGenerator of the new instance.
     * @return new instance.
     */
    public UuidConfiguration withUuidGenerator(String uuidGenerator) {
    	return new UuidConfiguration(getInclusionPolicies(), uuidGenerator, getUuidProperty(), shouldStripHyphens(), getUuidIndex(), getUuidRelationshipIndex(), getImmutable());
    }
    
    /**
     * Create a new instance of this {@link UuidConfiguration} with different uuid property.
     *
     * @param uuidProperty of the new instance.
     * @return new instance.
     */
    public UuidConfiguration withUuidProperty(String uuidProperty) {
        return new UuidConfiguration(getInclusionPolicies(), getUuidGenerator(), uuidProperty, shouldStripHyphens(), getUuidIndex(), getUuidRelationshipIndex(), getImmutable());
    }

    /**
     * Create a new instance of this {@link UuidConfiguration} with different uuid index.
     *
     * @param uuidIndex of the new instance.
     * @return new instance.
     */
    public UuidConfiguration withUuidIndex(String uuidIndex) {
        return new UuidConfiguration(getInclusionPolicies(), getUuidGenerator(), getUuidProperty(), shouldStripHyphens(), uuidIndex, getUuidRelationshipIndex(), getImmutable());
    }

    /**
     * Create a new instance of this {@link UuidConfiguration} with different uuid relationship index.
     *
     * @param uuidRelationshipIndex of the new instance.
     * @return new instance.
     */
    public UuidConfiguration withUuidRelationshipIndex(String uuidRelationshipIndex) {
        return new UuidConfiguration(getInclusionPolicies(), getUuidGenerator(), getUuidProperty(), shouldStripHyphens(), getUuidIndex(), uuidRelationshipIndex, getImmutable());
    }

    /**
     * Create a new instance of this {@link UuidConfiguration} with different uuid index.
     *
     * @param stripHyphens property of new instance.
     * @return new instance.
     */
    public UuidConfiguration withStripHyphensProperty(boolean stripHyphens) {
        return new UuidConfiguration(getInclusionPolicies(), getUuidGenerator(), getUuidProperty(), stripHyphens, getUuidIndex(), getUuidRelationshipIndex(), getImmutable());
    }

    /**
     * Create a new instance of this {@link UuidConfiguration} with a custom immutability setting.
     *
     * @param immutability property of new instance.
     * @return new instance.
     */
    public UuidConfiguration withImmutability(boolean immutability) {
        return new UuidConfiguration(getInclusionPolicies(), getUuidGenerator(), getUuidProperty(), shouldStripHyphens(), getUuidIndex(), getUuidRelationshipIndex(), immutability);
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

        UuidConfiguration that = (UuidConfiguration) o;

        if (!uuidProperty.equals(that.uuidProperty)) {
            return false;
        }
        if (!uuidIndex.equals(that.uuidIndex)) {
            return false;
        }

        if (!uuidRelationshipIndex.equals(that.uuidRelationshipIndex)) {
            return false;
        }

        if (stripHyphens != that.stripHyphens) {
            return false;
        }

        if (immutable != that.immutable) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + uuidGenerator.hashCode();
        result = 31 * result + uuidProperty.hashCode();
        result = 31 * result + uuidIndex.hashCode();
        result = 31 * result + uuidRelationshipIndex.hashCode();
        result = 31 * result + (stripHyphens ? 1 : 0);
        result = 31 * result + (immutable ? 1 : 0);
        return result;
    }
}
