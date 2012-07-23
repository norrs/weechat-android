/*******************************************************************************
 * Copyright 2012 Keith Johnson
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.ubergeek42.WeechatAndroid;

import java.util.*;
import java.util.Map.Entry;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.TextView;

import com.ubergeek42.WeechatAndroid.service.RelayServiceBinder;
import com.ubergeek42.weechat.Buffer;
import com.ubergeek42.weechat.relay.messagehandler.BufferManager;
import com.ubergeek42.weechat.relay.messagehandler.BufferManagerObserver;

public class BufferListAdapter implements BufferManagerObserver, OnSharedPreferenceChangeListener, ExpandableListAdapter {
	Activity parentActivity;
	LayoutInflater inflater;
	private BufferManager bufferManager;
    protected ArrayList<Entry<String, ArrayList<Buffer>>> groupBuffers;
    private final DataSetObservable dataSetObservable = new DataSetObservable();
	private SharedPreferences prefs;
	private boolean enableBufferSorting;
    private static final String TAG = "WeeBufferListAdapter";

    public BufferListAdapter(Activity parentActivity, RelayServiceBinder rsb) {
        this.parentActivity = parentActivity;
		this.inflater = LayoutInflater.from(parentActivity);

        groupBuffers = new ArrayList<Entry<String, ArrayList<Buffer>>>();
		prefs = PreferenceManager.getDefaultSharedPreferences(parentActivity.getBaseContext());
	    prefs.registerOnSharedPreferenceChangeListener(this);
	    enableBufferSorting = prefs.getBoolean("sort_buffers", true);
		
		bufferManager = rsb.getBufferManager();
		bufferManager.onChanged(this);
	}

    public void add(Entry<String, ArrayList<Buffer>> group) {
        this.groupBuffers.add(group);
        this.notifyDataSetChanged();
    }

    public void remove(String group) {
        for (Entry<String, ArrayList<Buffer>> entry : this.groupBuffers) {
            if (entry != null && entry.getKey().equals(group)) {
                this.groupBuffers.remove(group);
                this.notifyDataSetChanged();
                break;
            }
        }
    }

    public void remove(Entry<String, List<Buffer>> entry) {
        remove(entry.getKey());
    }

    public void addChild(String group, Buffer child) {
        for (Entry<String, ArrayList<Buffer>> entry : this.groupBuffers) {
            if (entry != null && entry.getKey().equals(group)) {
                if (entry.getValue() == null)
                    entry.setValue(new ArrayList<Buffer>());

                entry.getValue().add(child);
                this.notifyDataSetChanged();
                break;
            }
        }
    }

    public void removeChild(String group, Buffer child) {
        for (Entry<String, ArrayList<Buffer>> entry : this.groupBuffers) {
            if (entry != null && entry.getKey().equals(group)) {
                if (entry.getValue() == null)
                    return;

                entry.getValue().remove(child);
                this.notifyDataSetChanged();
                break;
            }
        }
    }

    @Override
    public void onBuffersChanged() {
        parentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                groupBuffers.clear();
                ArrayList<Entry<String, ArrayList<Buffer>>> groupBuffersUpdate = new ArrayList<Entry<String, ArrayList<Buffer>>>();
                for (Buffer buffer : bufferManager.getBuffers()) {
                    // Check if buffer's fullname contains at least 1 dot
                    // (makes sure it's not main buffer (weechat) or iset, grep etc.. )

                    String fullBufferName = buffer.getFullName().trim();
                    if (fullBufferName.contains(".")) {
                        String[] bufferGroups = fullBufferName.split("\\.");
                        String prefixGroup;
                        if (bufferGroups[0].equals("irc"))
                            prefixGroup = bufferGroups[1];
                        else
                            prefixGroup = bufferGroups[0];

                        boolean hasFound = false;
                        for (Iterator<Entry<String, ArrayList<Buffer>>> iterator = groupBuffers.iterator(); iterator.hasNext(); ) {
                            Entry<String, ArrayList<Buffer>> next = iterator.next();
                            if (next.getKey().equals(prefixGroup)) {
                                next.getValue().add(buffer);
                                hasFound = true;
                                break;
                            }
                        }
                        if (!hasFound) {
                            add(new AbstractMap.SimpleEntry<String, ArrayList<Buffer>>(prefixGroup, new ArrayList<Buffer>(Arrays.asList(new Buffer[]{buffer}))));
                        }

                        //Log.d(TAG, String.format("Adding %s to %s", fullBufferName, prefixGroup));
                    }

                }
                //buffers = bufferManager.getBuffers();
                // Sort buffers based on unread count
                /*if (enableBufferSorting) {
                    Collections.sort(buffers, bufferComparator);
                }*/
                notifyDataSetChanged();
            }
        });
    }
    private Comparator<Buffer> bufferComparator = new Comparator<Buffer>() {
        @Override
        public int compare(Buffer b1, Buffer b2) {
            int b1Highlights = b1.getHighlights();
            int b2Highlights = b2.getHighlights();
            if(b2Highlights > 0 || b1Highlights > 0) {
                return b2Highlights - b1Highlights;
            }
            return b2.getUnread() - b1.getUnread();
        }
    };



    static class ViewHolder {
        TextView shortname;
        TextView fullname;
        TextView hotlist;
        TextView title;
    }



    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("sort_buffers")) {
            enableBufferSorting = prefs.getBoolean("sort_buffers", true);
            onBuffersChanged();
        }
    }

    protected DataSetObservable getDataSetObservable() {
        return dataSetObservable;
    }

    public void notifyDataSetChanged() {
        this.getDataSetObservable().notifyChanged();
    }

    public void notifyDataSetInvalidated() {
        this.getDataSetObservable().notifyInvalidated();
    }

    public void registerDataSetObserver(DataSetObserver observer) {
        this.getDataSetObservable().registerObserver(observer);
    }

    public void unregisterDataSetObserver(DataSetObserver observer) {
        this.getDataSetObservable().unregisterObserver(observer);
    }

    @Override
    public int getGroupCount() {
        return groupBuffers.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return groupBuffers.get(groupPosition).getValue().size();
    }

    @Override
    public String getGroup(int groupPosition) {
        return groupBuffers.get(groupPosition).getKey();
    }

    @Override
    public Buffer getChild(int groupPosition, int childPosition) {
        return groupBuffers.get(groupPosition).getValue().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return ((Integer)groupPosition).longValue();
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return ((Integer)childPosition).longValue();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.nicklist_item, null); // android default text item is again?
            holder.title = (TextView) convertView.findViewById(R.id.nicklist_nick);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.title.setText(groupBuffers.get(groupPosition).getKey());
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.bufferlist_item, null);
            holder = new ViewHolder();
            holder.shortname = (TextView) convertView.findViewById(R.id.bufferlist_shortname);
            holder.fullname = (TextView) convertView.findViewById(R.id.bufferlist_fullname);
            holder.hotlist = (TextView) convertView.findViewById(R.id.bufferlist_hotlist);
            holder.title = (TextView) convertView.findViewById(R.id.bufferlist_title);

            convertView.setTag(holder);
            //convertView.setTag(getObjects().get(groupPosition).getValue().get(childPosition));
        } else {
            holder = (ViewHolder) convertView.getTag();

        }

        Buffer bufferItem = groupBuffers.get(groupPosition).getValue().get(childPosition);

        // use contents of bufferItem to fill in text content
        holder.fullname.setText(bufferItem.getFullName());
        holder.shortname.setText(bufferItem.getShortName());
        if (bufferItem.getShortName() == null)
            holder.shortname.setText(bufferItem.getFullName());

        // Title might be removed in different layouts
        if(holder.title!=null)
            holder.title.setText(com.ubergeek42.weechat.Color.stripAllColorsAndAttributes(bufferItem.getTitle()));

        int unread = bufferItem.getUnread();
        int highlight = bufferItem.getHighlights();
        holder.hotlist.setText(String.format("U:%2d  H:%2d   ", unread, highlight));

        if (highlight > 0) {
            holder.hotlist.setTextColor(Color.MAGENTA);
        } else if (unread > 0) {
            holder.hotlist.setTextColor(Color.YELLOW);
        } else {
            holder.hotlist.setTextColor(Color.WHITE);
        }

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEmpty() {
        return groupBuffers.size() == 0;
    }

    @Override
    public void onGroupExpanded(int groupPosition) {

    }

    @Override
    public void onGroupCollapsed(int groupPosition) {

    }

    public long getCombinedChildId(long groupId, long childId) {
        return groupId * 10000L + childId;
    }

    public long getCombinedGroupId(long groupId) {
        return groupId * 10000L;
    }


}
