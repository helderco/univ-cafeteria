
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

package pt.uac.cafeteria.model.validation;

import pt.uac.cafeteria.model.domain.Address;

/**
 * Validates Address domain objects.
 * <p>
 * It's possible to override the postal code regular expression.
 */
public class AddressValidator extends Validator<Address> {

    /** Regular expression for postal code validation. */
    protected String regexPostalCode = "^[0-9]{4}-[0-9]{3}$";

    /**
     * Sets a different regular expression for postal code validation.
     *
     * @param regex new regular expression.
     */
    public void setPostalCodePattern(String regex) {
        regexPostalCode = regex;
    }

    @Override
    public void doAssertions(Address address) {
        assertRequired("morada", address.getStreetAddress());
        assertRequired("número da porta", address.getNumber());

        if (assertRequired("código postal", address.getPostalCode())) {
            assertPostalCode(address.getPostalCode());
        }

        assertRequired("localidade", address.getCity());
    }

    /**
     * Asserts if a postal code is in a valid format.
     * <p>
     * Accepted by default: Portuguese format (e.g.: 9600-508).
     * <p>
     * Note: can be override by <code>setPostalCodePattern(newRegex)</code>.
     *
     * @param postalCode the postal code value.
     */
    protected void assertPostalCode(String postalCode) {
        boolean format = matchPattern(regexPostalCode, postalCode);
        check(format, "Código de postal com formato inválido.");
    }
}
