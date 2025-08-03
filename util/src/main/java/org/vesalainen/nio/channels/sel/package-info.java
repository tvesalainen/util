/*
 * Copyright (C) 2017 Timo Vesalainen <timo.vesalainen@iki.fi>
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

/**
 * Classes to implement replacement for SelectableChannel etc.
 * <p>
 * Classes in this package don't use OS level selectors. Similar behavior is
 * simulated. 
 * <p>
 * Reason for using this package might be that you have channels from different
 * selector providers.
 * <p>
 * Besides simulated select/selectedKeys loop, you can use faster forEach method.
 */
package org.vesalainen.nio.channels.sel;

