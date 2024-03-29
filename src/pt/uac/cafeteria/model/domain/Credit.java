
/**
 * Cafeteria management application
 * Copyright (c) 2011, 2012 Helder Correia, Nuno Silva
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

package pt.uac.cafeteria.model.domain;

/**
 * A type of transaction used when a student makes a payment to an
 * administrator to add credit to his account (increasing balance).
 * <p>
 * Only an administrator should be allowed to make this transaction
 * in a backend.
 */
public class Credit extends Transaction {

    /** Serialization version. */
    private static final long serialVersionUID = 1L;

    /** Administrator handling the transaction. */
    private final String administrator;

    /**
     * Creates a new Credit transaction.
     *
     * @param administrator the administrator handling the transaction.
     * @param amount the amount to add to the account balance.
     */
    public Credit(String administrator, double amount) {
        super(amount);
        this.administrator = administrator;
    }

    /** Gets the administrator responsible for the transaction. */
    public String getAdministrator() {
        return administrator;
    }

    @Override
    public String toString() {
        return "\n    Credit {"
            + "\n      date = " + getDate()
            + "\n      amount = " + getAmount()
            + "\n      administrator = " + getAdministrator()
            + "\n    }";
    }
}
