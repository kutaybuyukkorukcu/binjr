/*
 *    Copyright 2019 Frederic Thevenet
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package eu.binjr.core.data.adapters;

import eu.binjr.common.preferences.Preference;
import eu.binjr.common.preferences.PreferenceFactory;
import eu.binjr.core.preferences.UserPreferences;

import java.util.prefs.Preferences;

public class DataAdapterPreferences extends PreferenceFactory {

    public final Preference<Boolean> enabled = booleanPreference("adapterEnabled", true);

    public DataAdapterPreferences(Class<? extends DataAdapter> dataAdapterClass) {
        super(Preferences.userRoot().node(UserPreferences.BINJR_GLOBAL + "/adapters/" + dataAdapterClass.getName()));
    }
}
