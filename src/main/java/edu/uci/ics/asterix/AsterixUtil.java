/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package edu.uci.ics.asterix;

import com.fasterxml.jackson.databind.ObjectMapper;

public class AsterixUtil {
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static final String DATATYPE_QUERY = "SELECT VALUE x FROM Metadata.`Datatype` x;";
    public static final String DATASET_QUERY = "SELECT VALUE x FROM Metadata.`Dataset` x;";
    public static final String H_DESCRIPTION = "Help";
    public static final String W_DESCRIPTION = "format: server port dataverseName datasetName fileName\n Write a JSON file for the user.";
    public static final String R_DESCRIPTION = "format: server port dataverseName datasetName fileName\n Read the user specified PKs.";
    public static final String PROJECT_NAME = "nfview2";


    public static final String POS = "_pos";
    public static final String RECORD = "RECORD";
    public static final String FLAT = "FLAT";
    public static final String ANON = "_Anon";
    public static final String AT = "AT";
    public static final String USE = "USE";
    public static final String INNER_LIST = "_InnerList";
    public static final String AS = "AS";
    public static final String ORDERED_LIST = "ORDEREDLIST";

    public static final String NAME = "name";
    public static final String TYPE = "type";
    public static final String PRIMARY_KEY = "primaryKey";
    public static final String NESTED_FIELDS = "nestedFields";

    private AsterixUtil() {
    }
}
