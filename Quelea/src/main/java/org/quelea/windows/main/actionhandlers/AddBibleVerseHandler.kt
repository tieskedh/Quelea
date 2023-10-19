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
package org.quelea.windows.main.actionhandlers

import org.quelea.data.bible.Bible
import org.quelea.data.bible.BibleVerse
import org.quelea.data.displayable.BiblePassage
import org.quelea.windows.main.QueleaApp
import org.quelea.windows.main.schedule.ScheduleList


/**
 *
 * @author Arvid
 */
class AddBibleVerseHandler {
    fun add() {
        val sl = QueleaApp.get().mainWindow.mainPanel.schedulePanel.scheduleList
        val passage = QueleaApp.get().mainWindow.mainPanel.livePanel.displayable
                as? BiblePassage ?: return

        val firstVerse = passage.verses.first()
        val bible = firstVerse.chapter.book.bible

        val theme = passage.theme

        val newVerses = passage.verses.toMutableList()
        val lastOldVerse = newVerses.last()!!

        val lastOldVerseNum = lastOldVerse.num
        val lastOldChapterNum = lastOldVerse.chapter.num
        val lastOldBookNum = lastOldVerse.chapter.book.bookNumber

        val newVerse = bible[
            lastOldBookNum - 1,
            lastOldChapterNum - 1,
            lastOldVerseNum + 1
        ] ?: bible[
            lastOldBookNum - 1,
            lastOldChapterNum,
            1
        ]!!

        newVerses+=newVerse

        val passageNumber = getPassageNumber(
            passage,
            if (lastOldChapterNum == newVerse.chapter.num) 0 else newVerse.chapter.num,
            newVerse.num
        )
        val summary = "${firstVerse!!.chapter.book} $passageNumber\n${bible.bibleName}"

        sl.replacePassage(
            passage,
            BiblePassage(summary, newVerses.toTypedArray<BibleVerse?>(), theme, passage.multi)
        )
    }

    private fun ScheduleList.replacePassage(
        old: BiblePassage,
        new  : BiblePassage
    ) {
        selectionModel.clearSelection()
        val index = items.indexOf(old)

        if (index != -1) {
            items.removeAt(index)
            items.add(index, new)
            selectionModel.clearAndSelect(index)
        }
    }
    companion object {

        private operator fun Bible.get(bookNum : Int, chapterNum : Int, verseNum : Int) : BibleVerse? =
            this.bookList[bookNum].getChapter(chapterNum)!!.getVerse(verseNum)

        private fun getPassageNumber(passage: BiblePassage, chapter: Int, lastNumber: Int): String {
            var passageNumber = passage.location.split(Regex(" (?=\\d)"))
                .dropLastWhile { it.isEmpty() }[1]

            passageNumber = when {
                chapter > 0 -> "$passageNumber;$chapter:$lastNumber"
                ";" in passageNumber -> when {
                    "-" in passageNumber.substringAfterLast(';') ->
                        passageNumber.substringBeforeLast('-') + "-$lastNumber"

                    else -> "$passageNumber-$lastNumber"
                }

                "," in passageNumber -> when {
                    "-" in passageNumber.substringAfterLast(',') ->
                        passageNumber.substringBeforeLast('-') + "-$lastNumber"

                    else -> "$passageNumber-$lastNumber"
                }

                else -> when {
                    "-" in passageNumber ->
                        passageNumber.substringBeforeLast('-') + "-$lastNumber"

                    ":" in passageNumber -> "$passageNumber-$lastNumber"
                    else -> passageNumber
                }
            }
            return passageNumber
        }
    }
}
