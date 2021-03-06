/*
 * File    : GlobalManagerStatsImpl.java
 * Created : 21-Oct-2003
 * By      : stuff
 * 
 * Azureus - a Java Bittorrent client
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details ( see the LICENSE file ).
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.gudy.azureus2.core3.global.impl;

/**
 * @author parg
 *
 */

import org.gudy.azureus2.core3.config.COConfigurationManager;
import org.gudy.azureus2.core3.global.GlobalManagerStats;
import org.gudy.azureus2.core3.util.Average;


public class 
GlobalManagerStatsImpl
	implements GlobalManagerStats
{
	private final GlobalManagerImpl		manager;
	
	private long total_data_bytes_received;
    private long total_protocol_bytes_received;
    
	private long totalDiscarded;
    
    private long total_data_bytes_sent;
    private long total_protocol_bytes_sent;

    private int	data_send_speed_at_close;
    
	private final Average data_receive_speed = Average.getInstance(1000, 10);  //average over 10s, update every 1000ms
    private final Average protocol_receive_speed = Average.getInstance(1000, 10);  //average over 10s, update every 1000ms
	private final Average data_receive_speed_no_lan = Average.getInstance(1000, 10);  //average over 10s, update every 1000ms
    private final Average protocol_receive_speed_no_lan = Average.getInstance(1000, 10);  //average over 10s, update every 1000ms

	private final Average data_send_speed = Average.getInstance(1000, 10);  //average over 10s, update every 1000ms
    private final Average protocol_send_speed = Average.getInstance(1000, 10);  //average over 10s, update every 1000ms
	private final Average data_send_speed_no_lan = Average.getInstance(1000, 10);  //average over 10s, update every 1000ms
    private final Average protocol_send_speed_no_lan = Average.getInstance(1000, 10);  //average over 10s, update every 1000ms

    //********************************************

    /*
     * These values are not exact. In particular, we aren't prevent concurrent
     * access (e.g., via {@code AtomicLong}). This is a deliberate choice to
     * avoid
     * the overhead of locking.
     */

    private long f2f_bytes_received;
    private long f2f_bytes_sent;

    private long textSearchesReceived = 0;
    private long hashSearchesReceived = 0;
    private long idPrefixSearchesReceived = 0;
    private long e2dkPrefixSearchesReceived = 0;
    private long sha1PrefixSearchesReceived = 0;

    long textSearchesSent = 0;
    long hashSearchesSent = 0;
    long searchCancelsSent = 0;

    public long getTotalF2FBytesSent() {
    	return f2f_bytes_sent;
    }

    public long getTotalF2FBytesReceived() {
    	return f2f_bytes_received;
    }
    
    public void f2fBytesSent(int length) {
  		f2f_bytes_sent += length;
  	}
  	
    public void f2fBytesReceived(int length) {
  		f2f_bytes_received += length;
  	}

    public void textSearchReceived() {
        textSearchesReceived++;
    }

    public void hashSearchReceived() {
        hashSearchesReceived++;
    }

    public long getTextSearchesReceived() {
        return textSearchesReceived;
    }

    public long getHashSearchesReceived() {
        return hashSearchesReceived;
    }
  	//********************************************

    
	protected 
	GlobalManagerStatsImpl(
		GlobalManagerImpl	_manager )
	{
		manager = _manager;
		
		load();
	}
  
	protected void
	load()
	{
		data_send_speed_at_close	= COConfigurationManager.getIntParameter( "globalmanager.stats.send.speed.at.close", 0 );
	}
	
	protected void
	save()
	{
		COConfigurationManager.setParameter( "globalmanager.stats.send.speed.at.close", getDataSendRate());
	}
    
    public int 
	getDataSendRateAtClose()
	{
		return( data_send_speed_at_close );
	}
	
  			// update methods

    public void discarded(int length) {
		this.totalDiscarded += length;
	}

    public void dataBytesReceived(int length,boolean LAN){
		total_data_bytes_received += length;
		if ( !LAN ){
			data_receive_speed_no_lan.addValue(length);
		}
		data_receive_speed.addValue(length);
	}


    public void protocolBytesReceived(int length, boolean LAN ){
		total_protocol_bytes_received += length;
		if ( !LAN ){
			protocol_receive_speed_no_lan.addValue(length);
		}
		protocol_receive_speed.addValue(length);
	}

    public void dataBytesSent(int length, boolean LAN) {
		total_data_bytes_sent += length;
		if ( !LAN ){
			data_send_speed_no_lan.addValue(length);
		}
		data_send_speed.addValue(length);
	}

    public void protocolBytesSent(int length, boolean LAN) {
		total_protocol_bytes_sent += length;
		if ( !LAN ){
			protocol_send_speed_no_lan.addValue(length);
		}
		protocol_send_speed.addValue(length);
	}
	
    public int getDataReceiveRate() {
		return (int)data_receive_speed.getAverage();
	}
    public int getDataReceiveRateNoLAN() {
		return (int)data_receive_speed_no_lan.getAverage();
	}
    public int getDataReceiveRateNoLAN(int average_period) {
		return (int)(average_period<=0?data_receive_speed_no_lan.getAverage():data_receive_speed_no_lan.getAverage(average_period));
	}
    public int getProtocolReceiveRate() {
		return (int)protocol_receive_speed.getAverage();
	}
    public int getProtocolReceiveRateNoLAN() {
		return (int)protocol_receive_speed_no_lan.getAverage();
	}	
    public int getProtocolReceiveRateNoLAN(int average_period) {
		return (int)(average_period<=0?protocol_receive_speed_no_lan.getAverage():protocol_receive_speed_no_lan.getAverage(average_period));
	}

    public int getDataAndProtocolReceiveRate(){
		return((int)( protocol_receive_speed.getAverage() + data_receive_speed.getAverage()));
	}

    public int getDataSendRate() {
		return (int)data_send_speed.getAverage();
	}
    public int getDataSendRateNoLAN() {
		return (int)data_send_speed_no_lan.getAverage();
	}
    public int getDataSendRateNoLAN(int average_period) {
		return (int)(average_period<=0?data_send_speed_no_lan.getAverage():data_send_speed_no_lan.getAverage(average_period));
	}
	
    public int getProtocolSendRate() {
		return (int)protocol_send_speed.getAverage();
	}
    public int getProtocolSendRateNoLAN() {
		return (int)protocol_send_speed_no_lan.getAverage();
	}
    public int getProtocolSendRateNoLAN(int average_period) {
		return (int)(average_period<=0?protocol_send_speed_no_lan.getAverage():protocol_send_speed_no_lan.getAverage(average_period));
	}

    public int getDataAndProtocolSendRate(){
		return((int)( protocol_send_speed.getAverage() + data_send_speed.getAverage()));
	}

    public long getTotalDataBytesSent() {
    	return total_data_bytes_sent;
    }

    public long getTotalProtocolBytesSent() {
    	return total_protocol_bytes_sent;
    }


    public long getTotalDataBytesReceived() {
    	return total_data_bytes_received;
    }

    public long getTotalProtocolBytesReceived() {
    	return total_protocol_bytes_received;
    }


    public long getTotalDiscardedRaw() {
    	return totalDiscarded;
    }

    public long getTotalSwarmsPeerRate(boolean downloading, boolean seeding )
    {
    	return( manager.getTotalSwarmsPeerRate(downloading,seeding));
    }

    public void sha1PrefixSearchReceived() {
        sha1PrefixSearchesReceived++;
    }

    public void ed2kPrefixSearchReceived() {
        e2dkPrefixSearchesReceived++;
    }

    public void idPrefixSearchReceived() {
        idPrefixSearchesReceived++;
    }

    public long getIdPrefixSearchesReceived() {
        return idPrefixSearchesReceived;
    }

    public long getE2dkPrefixSearchesReceived() {
        return e2dkPrefixSearchesReceived;
    }

    public long getSha1PrefixSearchesReceived() {
        return sha1PrefixSearchesReceived;
    }

    public void textSearchSent() {
        textSearchesSent++;
    }

    public void hashSearchSent() {
        hashSearchesSent++;
    }

    public void searchCancelSent() {
        searchCancelsSent++;
    }

    public long getTextSearchesSent() {
        return textSearchesSent;
    }

    public long getHashSearchesSent() {
        return hashSearchesSent;
    }

    public long getSearchCancelsSent() {
        return searchCancelsSent;
    }
}
