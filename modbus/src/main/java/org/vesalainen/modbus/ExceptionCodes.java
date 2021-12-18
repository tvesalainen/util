/*
 * Copyright (C) 2021 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.modbus;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public enum ExceptionCodes
{
    RESERVED_00,
    /**
     * The function code received in the query is not an allowable action for
     * the server (or slave). This may be because the function code is only
     * applicable to newer devices, and was not implemented in the unit
     * selected. It could also indicate that the server (or slave) is in the
     * wrong state to process a request of this type, for example because it is
     * unconfigured and is being asked to return register values.
     */
    ILLEGAL_FUNCTION_01,
    /**
     * The data address received in the query is not an allowable address for
     * the server (or slave). More specifically, the combination of reference
     * number and transfer length is invalid. For a controller with 100
     * registers, the PDU addresses the first register as 0, and the last one as
     * 99. If a request is submitted with a starting register address of 96 and
     * a quantity of registers of 4, then this request will successfully operate
     * (address-wise at least) on registers 96, 97, 98, 99. If a request is
     * submitted with a starting register address of 96 and a quantity of
     * registers of 5, then this request will fail with Exception Code 0x02
     * “Illegal Data Address” since it attempts to operate on registers 96, 97,
     * 98, 99 and 100, and there is no register with address 100.
     */
    ILLEGAL_DATA_ADDRESS_02,
    /**
     * A value contained in the query data field is not an allowable value for
     * server (or slave). This indicates a fault in the structure of the
     * remainder of a complex request, such as that the implied length is
     * incorrect. It specifically does NOT mean that a data item submitted for
     * storage in a register has a value outside the expectation of the
     * application program, since the MODBUS protocol is unaware of the
     * significance of any particular value of any particular register. 04 SLAVE
     * DEVICE FAILURE An unrecoverable error occurred while the server (or
     * slave) was attempting to perform the requested action.
     */
    ILLEGAL_DATA_VALUE_03,
    /**
     * An unrecoverable error occurred while the server (or slave) was
     * attempting to perform the requested action.
     */
    SLAVE_DEVICE_FAILURE_04,
    /**
     * Specialized use in conjunction with programming commands. The server (or
     * slave) has accepted the request and is processing it, but a long duration
     * of time will be required to do so. This response is returned to prevent a
     * timeout error from occurring in the client (or master). The client (or
     * master) can next issue a Poll Program Complete message to determine if
     * processing is completed.
     */
    ACKNOWLEDGE_05,
    /**
     * Specialized use in conjunction with programming commands. The server (or
     * slave) is engaged in processing a long–duration program command. The
     * client (or master) should retransmit the message later when the server
     * (or slave) is free.
     */
    SLAVE_DEVICE_BUSY_06,
    RESERVED_07,
    /**
     * Specialized use in conjunction with function codes 20 and 21 and
     * reference type 6, to indicate that the extended file area failed to pass
     * a consistency check. MODBUS Application Protocol Specification V1.1b
     * Modbus-IDA December 28, 2006 http://www.Modbus-IDA.org 50/51 The server
     * (or slave) attempted to read record file, but detected a parity error in
     * the memory. The client (or master) can retry the request, but service may
     * be required on the server (or slave) device.
     */
    MEMORY_PARITY_ERROR_08,
    RESERVED_09,
    /**
     * Specialized use in conjunction with gateways, indicates that the gateway
     * was unable to allocate an internal communication path from the input port
     * to the output port for processing the request. Usually means that the
     * gateway is misconfigured or overloaded.
     */
    GATEWAY_PATH_UNAVAILABLE_0A,
    /**
     * Specialized use in conjunction with gateways, indicates that no response
     * was obtained from the target device. Usually means that the device is not
     * present on the network. }
     *
     */
    GATEWAY_TARGET_DEVICE_FAILED_TO_RESPOND_0B;

    static void values(byte exception)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
