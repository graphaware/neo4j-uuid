/*
 * Copyright (c) 2014 GraphAware
 *
 * This file is part of GraphAware.
 *
 * GraphAware is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
package com.graphaware.module.uuid;

import com.graphaware.common.strategy.InclusionStrategies;
import com.graphaware.runtime.config.BaseTxDrivenModuleConfiguration;
import com.graphaware.runtime.strategy.InclusionStrategiesFactory;

import java.util.Collections;
import java.util.List;


/**
 * {@link BaseTxDrivenModuleConfiguration} for {@link com.graphaware.module.uuid.UuidModule}.
 */
public class UuidConfiguration extends BaseTxDrivenModuleConfiguration<UuidConfiguration> {

    private static final String DEFAULT_UUID_PROPERTY = "uuid";

    private String uuidProperty;
    private List<String> labels;

    protected UuidConfiguration(InclusionStrategies inclusionStrategies) {
        super(inclusionStrategies);
    }

    public UuidConfiguration(InclusionStrategies inclusionStrategies, String uuidProperty, List<String> labels) {
        super(inclusionStrategies);
        this.uuidProperty = uuidProperty;
        this.labels = labels;
    }

    /**
     * Create a default configuration with default uuid property = {@link #DEFAULT_UUID_PROPERTY}, labels=all (including nodes with no labels)
     * inclusion strategies = {@link com.graphaware.runtime.strategy.InclusionStrategiesFactory#allBusiness()},
     * (nothing is excluded except for framework-internal nodes and relationships)
     * <p/>
     * Change this by calling {@link #withUuidProperty(String)}, with* other inclusion strategies
     * on the object, always using the returned object (this is a fluent interface).
     */
    public static UuidConfiguration defaultConfiguration() {
        return new UuidConfiguration(InclusionStrategiesFactory.allBusiness(), DEFAULT_UUID_PROPERTY, Collections.EMPTY_LIST);
    }

    @Override
    protected UuidConfiguration newInstance(InclusionStrategies inclusionStrategies) {
        return new UuidConfiguration(inclusionStrategies, getUuidProperty(), getLabels());
    }

    public String getUuidProperty() {
        return uuidProperty;
    }

    public List<String> getLabels() {
        return labels;
    }

    /**
     * Create a new instance of this {@link UuidConfiguration} with different uuid property.
     *
     * @param uuidProperty of the new instance.
     * @return new instance.
     */
    public UuidConfiguration withUuidProperty(String uuidProperty) {
        return new UuidConfiguration(getInclusionStrategies(), uuidProperty, getLabels());
    }

    /**
     * Create a new instance of this {@link UuidConfiguration} with different labels property.
     *
     * @param labels nodes with these labels will be assigned a UUID property
     * @return new instance.
     */
    public UuidConfiguration withLabels(List<String> labels) {
        return new UuidConfiguration(getInclusionStrategies(), getUuidProperty(), labels);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        UuidConfiguration that = (UuidConfiguration) o;

        if (labels != null ? !labels.equals(that.labels) : that.labels != null) return false;
        if (!uuidProperty.equals(that.uuidProperty)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + uuidProperty.hashCode();
        result = 31 * result + (labels != null ? labels.hashCode() : 0);
        return result;
    }
}
