package edu.asu.msse.emerginglanguages.data;

/**
 * Copyright 2015 Akshay Ashwathanarayana,
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p/>
 *
 * @author Akshay Ashwathanarayana mailto:Akshay.Ashwathanarayana@asu.edu
 * @version April 22, 2015
 */

public class Data {
	public DataType type;
	public String value;
	
	public Data(String value, DataType type) {
        this.value = value;
        this.type = type;
    }

    @Override
    public String toString() {
        return value;
    }
}
