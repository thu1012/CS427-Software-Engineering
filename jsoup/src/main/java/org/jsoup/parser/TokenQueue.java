package org.jsoup.parser;

import org.jsoup.helper.Validate;
import org.jsoup.internal.StringUtil;

/**
 * A character queue with parsing helpers.
 *
 * @author Jonathan Hedley
 */
public class TokenQueue {
    private String queue;
    private int pos = 0;
    
    private static final char ESC = '\\'; // escape char for chomp balanced.

    /**
     Create a new TokenQueue.
     @param data string of data to back queue.
     */
    public TokenQueue(String data) {
        Validate.notNull(data);
        queue = data;
    }

    /**
     * Is the queue empty?
     * @return true if no data left in queue.
     */
    public boolean isEmpty() {
        return remainingLength() == 0;
    }
    
    private int remainingLength() {
        return queue.length() - pos;
    }

    /**
     * Retrieves but does not remove the first character from the queue.
     * @return First character, or 0 if empty.
     */
    public char peek() {
        return isEmpty() ? 0 : queue.charAt(pos);
    }

    /**
     Add a character to the start of the queue (will be the next character retrieved).
     @param c character to add
     */
    public void addFirst(Character c) {
        addFirst(c.toString());
    }

    /**
     Add a string to the start of the queue.
     @param seq string to add.
     */
    public void addFirst(String seq) {
        // not very performant, but an edge case
        queue = seq + queue.substring(pos);
        pos = 0;
    }

    /**
     * Tests if the next characters on the queue match the sequence. Case insensitive.
     * @param seq String to check queue for.
     * @return true if the next characters match.
     */
    public boolean matches(String seq) {
        return queue.regionMatches(true, pos, seq, 0, seq.length());
    }

    /**
     * Case sensitive match test.
     * @param seq string to case sensitively check for
     * @return true if matched, false if not
     */
    public boolean matchesCS(String seq) {
        return queue.startsWith(seq, pos);
    }
    

    /**
     Tests if the next characters match any of the sequences. Case insensitive.
     @param seq list of strings to case insensitively check for
     @return true of any matched, false if none did
     */
    public boolean matchesAny(String... seq) {
        boolean flag = false;
        for (String s : seq) {
            if (matches(s)) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    /**
     * Tests if the next character matches any of the sequences. Case sensitive.
     * @param seq list of characters to case sensitively check for
     * @return true of any matched, false if none did
     */
    public boolean matchesAny(char... seq) {
        boolean flag = false;
        if(!isEmpty()) {
            for (char c: seq) {
                if (queue.charAt(pos) == c) {
                    flag = true;
                    break;
                }
            }
        }
        return flag;
    }

    /**
     * Tests if the next characters matches {@code <x} where x is a letter
     * @return true if the next characters indicate the start of a tag, false otherwise
     */
    public boolean matchesStartTag() {
        // micro opt for matching "<x"
        return (remainingLength() >= 2 
                && queue.charAt(pos) == '<' 
                && Character.isLetter(queue.charAt(pos+1)));
    }

    /**
     * Tests if the queue matches the sequence (as with match), and if they do, 
     * removes the matched string from the queue.
     * @param seq String to search for, and if found, remove from queue.
     * @return true if found and removed, false if not found.
     */
    public boolean matchChomp(String seq) {
        boolean flag = false;
        if (matches(seq)) {
            pos += seq.length();
            flag = true;
        } 
        return flag;
    }

    /**
     Tests if queue starts with a whitespace character.
     @return if starts with whitespace
     */
    public boolean matchesWhitespace() {
        return !isEmpty() && StringUtil.isWhitespace(queue.charAt(pos));
    }

    /**
     Test if the queue matches a word character (letter or digit).
     @return if matches a word character
     */
    public boolean matchesWord() {
        return !isEmpty() && Character.isLetterOrDigit(queue.charAt(pos));
    }

    /**
     * Drops the next character off the queue.
     */
    public void advance() {
        if (!isEmpty()) {
            pos++;
        }
    }

    /**
     * Consume one character off queue.
     * @return first character on queue.
     */
    public char consume() {
        return queue.charAt(pos++);
    }

    /**
     * Consumes the supplied sequence of the queue. If the queue does not start with the supplied 
     * sequence, will throw an illegal state exception -- but you should be running match() against 
     * that condition.
     <p>
     Case insensitive.
     * @param seq sequence to remove from head of queue.
     */
    public void consume(String seq) {
        if (!matches(seq)) {
            throw new IllegalStateException("Queue did not match expected sequence");
        }
        int len = seq.length();
        if (len > remainingLength()) {
            throw new IllegalStateException("Queue not long enough to consume sequence");
        }
        pos += len;
    }

    /**
     * Pulls a string off the queue, up to but exclusive of the match sequence, or to the queue 
     * running out.
     * @param seq String to end on (and not include in return, but leave on queue). 
     * <b>Case sensitive.</b>
     * @return The matched data consumed from queue.
     */
    public String consumeTo(String seq) {
        String res;
        int offset = queue.indexOf(seq, pos);
        if (offset != -1) {
            String consumed = queue.substring(pos, offset);
            pos += consumed.length();
            res = consumed;
        } else {
            res = remainder();
        }
        return res;
    }
    
    /**
     * Consumes to the first sequence provided, or to the end of the queue. Case incensitive.
     * @param seq terminator to consume to. Case insensitive.
     * @return consumed string
     */
    public String consumeToIgnoreCase(String seq) {
        int start = pos; 
        String first = seq.substring(0, 1);
        // if first is not cased, use index of
        boolean canScan = first.toLowerCase().equals(first.toUpperCase()); 
        while (!isEmpty()) {
            if (matches(seq)) {
                break;
            }
            
            if (canScan) {
                int skip = queue.indexOf(first, pos) - pos;
                // this char is the skip char, but not match, so force advance of pos
                if (skip == 0) {
                    pos++;
                }
                else if (skip < 0) { // no chance of finding, grab to end
                    pos = queue.length();
                }
                else {
                    pos += skip;
                }
            }
            else {
                pos++;
            }
        }

        return queue.substring(start, pos);
    }

    /**
     Consumes to the first sequence provided, or to the end of the queue. Leaves the terminator on 
     the queue.
     @param seq any number of terminators to consume to. <b>Case insensitive.</b>
     @return consumed string   
     */
    // todo: method name. not good that consumeTo cares for case, and consume to any doesn't. And 
    // the only use for this is is a case sensitive time...
    public String consumeToAny(String... seq) {
        int start = pos;
        while (!isEmpty() && !matchesAny(seq)) {
            pos++;
        }

        return queue.substring(start, pos);
    }

    /**
     * Pulls a string off the queue (like consumeTo), and then pulls off the matched string (but 
     * does not return it).
     * <p>
     * If the queue runs out of characters before finding the seq, will return as much as it can 
     * (and queue will go isEmpty() == true).
     * @param seq String to match up to, and not include in return, and to pull off queue. 
     * <b>Case sensitive.</b>
     * @return Data matched from queue.
     */
    public String chompTo(String seq) {
        String data = consumeTo(seq);
        matchChomp(seq);
        return data;
    }
    
    /**
     * Pulls a string off the queue (like consumeTo), and then pulls off the matched string (but 
     * does not return it).
     * <p>
     * If the queue runs out of characters before finding the seq, will return as much as it can 
     * (and queue will go isEmpty() == true).
     * @param seq String to match up to, and not include in return, and to pull off queue. 
     * <b>Case insensitive.</b>
     * @return Data matched from queue.
     */
    public String chompToIgnoreCase(String seq) {
        String data = consumeToIgnoreCase(seq); // case insensitive scan
        matchChomp(seq);
        return data;
    }

    private String validateBalancedString(int start, int end, int depth) {
        final String out = (end >= 0) ? queue.substring(start, end) : "";
        if (depth > 0) { // ran out of queue before seeing enough ) 
            Validate.fail("Did not find balanced marker at '" + out + "'");
        }
        return out;
    }
    
    /**
     * Pulls a balanced string off the queue. E.g. if queue is "(one (two) three) four", (,) will 
     * return "one (two) three", and leave " four" on the queue. Unbalanced openers and closers can 
     * be quoted (with ' or ") or escaped (with \). Those escapes will be left in the returned 
     * string, which is suitable for regexes (where we need to preserve the escape), but unsuitable 
     * for contains text strings; use unescape for that.
     * @param open opener
     * @param close closer
     * @return data matched from the queue
     */
    public String chompBalanced(char open, char close) {
        int start = -1;
        int end = -1;
        int depth = 0;
        char last = 0;
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        do {
            if (isEmpty()) { break; }
            Character c = consume();
            if (last == 0 || last != ESC) {
                if (c.equals('\'') && c != open && !inDoubleQuote) {
                    inSingleQuote = !inSingleQuote;
                }
                else if (c.equals('"') && c != open && !inSingleQuote) {
                    inDoubleQuote = !inDoubleQuote;
                }
                if (inSingleQuote || inDoubleQuote) { continue; }

                if (c.equals(open)) {
                    depth++;
                    if (start == -1) { start = pos; }
                }
                else if (c.equals(close)) { depth--; }
            }
            // don't include the outer match pair in the return
            if (depth > 0 && last != 0) { end = pos; }
            last = c;
        } while (depth > 0);
        return validateBalancedString(start, end, depth);
    }
    
    /**
     * Unescape a \ escaped string.
     * @param in backslash escaped string
     * @return unescaped string
     */
    public static String unescape(String in) {
        StringBuilder out = StringUtil.borrowBuilder();
        char last = 0;
        for (char c : in.toCharArray()) {
            if (c == ESC) {
                if (last != 0 && last == ESC) {
                    out.append(c);
                }
            }
            else {
                out.append(c);
            }
            last = c;
        }
        return StringUtil.releaseBuilder(out);
    }

    /**
     * Pulls the next run of whitespace characters of the queue.
     * @return Whether consuming whitespace or not
     */
    public boolean consumeWhitespace() {
        boolean seen = false;
        while (matchesWhitespace()) {
            pos++;
            seen = true;
        }
        return seen;
    }

    /**
     * Retrieves the next run of word type (letter or digit) off the queue.
     * @return String of word characters from queue, or empty string if none.
     */
    public String consumeWord() {
        int start = pos;
        while (matchesWord()) {
            pos++;
        }
        return queue.substring(start, pos);
    }
    
    /**
     * Consume an tag name off the queue (word or :, _, -)
     * 
     * @return tag name
     */
    public String consumeTagName() {
        int start = pos;
        while (!isEmpty() && (matchesWord() || matchesAny(':', '_', '-'))) {
            pos++;
        }
        
        return queue.substring(start, pos);
    }
    
    /**
     * Consume a CSS element selector (tag name, but | instead of : for namespaces (or *| for 
     * wildcard namespace), to not conflict with :pseudo selects).
     * 
     * @return tag name
     */
    public String consumeElementSelector() {
        int start = pos;
        while (!isEmpty() && (matchesWord() || matchesAny("*|","|", "_", "-"))) {
            pos++;
        }
        
        return queue.substring(start, pos);
    }

    /**
     Consume a CSS identifier (ID or class) off the queue (letter, digit, -, _)
     http://www.w3.org/TR/CSS2/syndata.html#value-def-identifier
     @return identifier
     */
    public String consumeCssIdentifier() {
        int start = pos;
        while (!isEmpty() && (matchesWord() || matchesAny('-', '_'))) {
            pos++; 
        }

        return queue.substring(start, pos);
    }

    /**
     Consume an attribute key off the queue (letter, digit, -, _, :")
     @return attribute key
     */
    public String consumeAttributeKey() {
        int start = pos;
        while (!isEmpty() && (matchesWord() || matchesAny('-', '_', ':'))) {
            pos++;
        }
        
        return queue.substring(start, pos);
    }

    /**
     Consume and return whatever is left on the queue.
     @return remained of queue.
     */
    public String remainder() {
        final String remainder = queue.substring(pos, queue.length());
        pos = queue.length();
        return remainder;
    }
    
    @Override
    public String toString() {
        return queue.substring(pos);
    }
}
