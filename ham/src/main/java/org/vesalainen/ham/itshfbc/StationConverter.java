/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.vesalainen.ham.itshfbc;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.vesalainen.ham.BroadcastStationsFile;
import org.vesalainen.ham.LocationParser;
import static org.vesalainen.ham.itshfbc.GeoSearch.of;
import org.vesalainen.ham.jaxb.StationType;
import org.vesalainen.util.navi.Location;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class StationConverter
{
    private enum State {EXP_TRANSMITTER, TRANSMITTER, SHCEDULE, AREA};
    private Path in;
    private Path out;
    private BroadcastStationsFile xml;
    private Map<String, Location> map;

    public StationConverter(Path in, Path out)
    {
        this.in = in;
        this.out = out;
    }

    public void convert() throws IOException
    {
        map = createStations();
        xml = new BroadcastStationsFile(out.toFile());
        try (BufferedReader br = Files.newBufferedReader(in))
        {
            State state = State.AREA;
            StationType station;
            String line = br.readLine();
            while (line != null)
            {
                switch (state)
                {
                    case
                    case AREA:
                        station = station(line);
                        if (station != null)
                        {
                            state = State.TRANSMITTER;
                        }
                        else
                        {
                            
                        }
                }
                line = br.readLine();
            }
        }
    }
    private StationType station(String line)
    {
        for (String name : map.keySet())
        {
            if (line.startsWith(name))
            {
                return xml.addStation(name, map.get(name));
            }
        }
    }
    private Map<String, Location> createStations() throws IOException
    {
        Map<String,Location> map = new HashMap<>();
        GeoDB db = new GeoDB();
        GeoLocation loc = db.search(100, of ("CITY", "CAPE TOWN"), of ("NATION", "SOUTH AFRICA"));
        map.put("CAPE NAVAL, SOUTH AFRICA", loc.getLocation());
        loc = db.search(100, of ("CITY", "TOKYO") , of ("NATION", "JAPAN"));
        map.put("TOKYO, JAPAN", loc.getLocation());
        loc = db.search(100, of ("CITY", "PEVEK") , of ("NATION", "RUSSIA"));
        map.put("PEVEK, CHUKOTKA PENINSULA", loc.getLocation());
        loc = db.search(100, of ("CITY", "TAIPEI") , of ("NATION", "TAIWAN"));
        map.put("TAIPEI, REPUBLIC OF CHINA", loc.getLocation());
        loc = db.search(100, of ("CITY", "SEOUL") , of ("NATION", "SOUTH KOREA"));
        map.put("SEOUL, REPUBLIC OF KOREA", loc.getLocation());
        loc = db.search(100, of ("CITY", "BANGKOK") , of ("NATION", "THAILAND"));
        map.put("BANGKOK, THAILAND", loc.getLocation());
        loc = db.search(100, of ("CITY", "TOKYO") , of ("NATION", "JAPAN"));
        map.put("KYODO NEWS AGENCY, JAPAN/SINGAPORE", loc.getLocation());
        loc = db.search(100, of ("CITY", "LONDON") , of ("NATION", "UNITED KINGDOM"));
        map.put("NORTHWOOD, UNITED KINGDOM (PERSIAN GULF)", loc.getLocation());
        loc = db.search(100, of ("CITY", "RIO DE JANEIRO") , of ("NATION", "BRAZIL"));
        map.put("RIO DE JANEIRO, BRAZIL", loc.getLocation());
        loc = db.search(100, of ("CITY", "VALPARAISO") , of ("NATION", "CHILE"));
        map.put("VALPARAISO PLAYA ANCHA, CHILE", loc.getLocation());
        loc = db.search(100, of ("CITY", "PUNTA ARENAS") , of ("NATION", "CHILE"));
        map.put("PUNTA ARENAS MAGALLANES, CHILE", loc.getLocation());
        loc = db.search(100, of ("CITY", "HALIFAX"), of("STATE", "NS") , of ("NATION", "CANADA"));
        map.put("HALIFAX, NOVA SCOTIA, CANADA", loc.getLocation());
        loc = db.search(100, of ("CITY", "FROBISHER BAY") , of ("NATION", "CANADA"));
        map.put("IQALUIT, CANADA", loc.getLocation());
        loc = db.search(100, of ("CITY", "RESOLUTE") , of ("NATION", "CANADA"));
        map.put("RESOLUTE, CANADA", loc.getLocation());
        loc = db.search(100, of ("CITY", "SYDNEY"), of("STATE", "NS") , of ("NATION", "CANADA"));
        map.put("SYDNEY - NOVA SCOTIA, CANADA", loc.getLocation());
        loc = db.search(100, of ("CITY", "INUVIK") , of ("NATION", "CANADA"));
        map.put("INUVIK, CANADA", loc.getLocation());
        loc = db.search(100, of ("CITY", "KODIAK"), of("STATE", "AK") , of ("NATION", "USA"));
        map.put("KODIAK, ALASKA, U.S.A.", loc.getLocation());
        loc = db.search(100, of ("CITY", "SAN FRANCISCO"), of("STATE", "CA") , of ("NATION", "USA"));
        map.put("PT. REYES, CALIFORNIA, U.S.A.", loc.getLocation());
        loc = db.search(100, of ("CITY", "NEW ORLEANS"), of("STATE", "LA") , of ("NATION", "USA"));
        map.put("NEW ORLEANS, LOUISIANA, U.S.A", loc.getLocation());
        loc = db.search(100, of ("CITY", "BOSTON"), of("STATE", "MA") , of ("NATION", "USA"));
        map.put("BOSTON, MASSACHUSETTS, U.S.A.", loc.getLocation());
        map.put("CHARLEVILLE, AUSTRALIA", LocationParser.parse("26°19'49''S 146°15'51''E"));
        map.put("WILUNA, AUSTRALIA", LocationParser.parse("26°20'27''S 120°33'24''E "));
        loc = db.search(100, of ("CITY", "WELLINGTON") , of ("NATION", "NEW ZEALAND"));
        map.put("WELLINGTON, NEW ZEALAND", loc.getLocation());
        loc = db.search(100, of ("CITY", "HONOLULU"), of ("NATION", "USA"));
        map.put("HONOLULU, HAWAII, U.S.A.", loc.getLocation());
        loc = db.search(100, of ("CITY", "ATHENS") , of ("NATION", "GREECE"));
        map.put("ATHENS, GREECE", loc.getLocation());
        loc = db.search(100, of ("CITY", "MURMANSK") , of ("NATION", "RUSSIA"));
        map.put("MURMANSK, RUSSIA", loc.getLocation());
        loc = db.search(100, of ("CITY", "HAMBURG") , of ("NATION", "GERMANY"));
        map.put("HAMBURG/PINNEBERG, GERMANY", loc.getLocation());
        loc = db.search(100, of ("CITY", "LONDON") , of ("NATION", "UNITED KINGDOM"));
        map.put("NORTHWOOD, UNITED KINGDOM", loc.getLocation());
        return map;
    }
}
