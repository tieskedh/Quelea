/* 
 * This file is part of Quelea, free projection software for churches.
 * 
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
package org.quelea.data.bible

/**
 *
 * @author Ben Goodwin
 */
interface BibleInterface {
    /**
     * Returns the number associated - book/chapter/verse only
     *
     *
     * @return book/chapter/verse number
     */
    val num: Int

    /**
     * Returns Name of the bible section. Bible title, book name, chapter first verse.
     *
     *
     *
     * @return Name of the bible/book/chapter
     */
    val name: String

    /**
     * Returns the text associated with the bible section
     *
     *
     *
     * @return Text of the whole bible section
     */
    val text: String

    /**
     * Returns the bible section containing this item
     *
     *
     * @return bible/book/chapter
     */
    val parent: BibleInterface?
}
