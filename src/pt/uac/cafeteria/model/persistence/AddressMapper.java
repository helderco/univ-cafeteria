
/**
 * Cafeteria management application
 * Copyright (c) 2011, 2012 Helder Correia
 * 
 * This file is part of Cafeteria.
 * 
 * Cafeteria is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * Cafeteria is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cafeteria.  If not, see <http://www.gnu.org/licenses/>.
 */

package pt.uac.cafeteria.model.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import pt.uac.cafeteria.model.domain.Address;
import pt.uac.cafeteria.model.persistence.abstracts.DatabaseMapper;

/**
 * Data Mapper for Address domain objects.
 */
public class AddressMapper extends DatabaseMapper<Address> {

    /**
     * Creates a new AddressMapper instance.
     *
     * @param con a database connection object.
     */
    public AddressMapper(Connection con) {
        super(con);
    }

    @Override
    protected String table() {
        return "Moradas";
    }

    @Override
    protected String findStatement() {
        return "SELECT id, rua, nr, cod_postal, localidade"
                + " FROM " + table() + " WHERE id = ?";
    }

    @Override
    protected Address doLoad(Integer id, ResultSet rs) throws SQLException {
        String streetAddress = rs.getString("rua");
        String number = rs.getString("nr");
        String postalCode = rs.getString("cod_postal");
        String city = rs.getString("localidade");
        Address result = new Address(streetAddress, number, postalCode, city);
        result.setId(id);
        return result;
    }

    @Override
    protected String insertStatement() {
        return "INSERT INTO " + table()
                + " (rua, nr, cod_postal, localidade) VALUES (?, ?, ?, ?)";
    }

    @Override
    protected void doInsert(Address address, PreparedStatement stmt) throws SQLException {
        stmt.setString(1, address.getStreetAddress());
        stmt.setString(2, address.getNumber());
        stmt.setString(3, address.getPostalCode());
        stmt.setString(4, address.getCity());
    }

    @Override
    protected String updateStatement() {
        return "UPDATE " + table()
                + " SET rua = ?, nr = ?, cod_postal = ?, localidade = ?"
                + " WHERE id = ?";
    }

    @Override
    protected void doUpdate(Address address, PreparedStatement stmt) throws SQLException {
        doInsert(address, stmt);
        stmt.setInt(5, address.getId().intValue());
    }
 }
