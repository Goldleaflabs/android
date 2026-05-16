<?php
/*
 * mbstring_fallback.inc
 * Provides fallback implementations for mbstring functions
 * Used when the mbstring PHP extension is not available
 */

// mb_strlen - get string length
if (!function_exists('mb_strlen')) {
    function mb_strlen($string, $encoding = null) {
        if ($encoding && function_exists('iconv_strlen')) {
            return iconv_strlen($string, $encoding);
        }
        return strlen($string);
    }
}

// mb_substr - extract part of string
if (!function_exists('mb_substr')) {
    function mb_substr($string, $start, $length = null, $encoding = null) {
        if ($encoding && function_exists('iconv_substr')) {
            return iconv_substr($string, $start, $length, $encoding);
        }
        if ($length === null) {
            return substr($string, $start);
        }
        return substr($string, $start, $length);
    }
}

// mb_strpos - find position of first occurrence
if (!function_exists('mb_strpos')) {
    function mb_strpos($haystack, $needle, $offset = 0, $encoding = null) {
        return strpos($haystack, $needle, $offset);
    }
}

// mb_stripos - find position of first occurrence (case-insensitive)
if (!function_exists('mb_stripos')) {
    function mb_stripos($haystack, $needle, $offset = 0, $encoding = null) {
        return stripos($haystack, $needle, $offset);
    }
}

// mb_strrpos - find position of last occurrence
if (!function_exists('mb_strrpos')) {
    function mb_strrpos($haystack, $needle, $offset = 0, $encoding = null) {
        return strrpos($haystack, $needle, $offset);
    }
}

// mb_strripos - find position of last occurrence (case-insensitive)
if (!function_exists('mb_strripos')) {
    function mb_strripos($haystack, $needle, $offset = 0, $encoding = null) {
        return strripos($haystack, $needle, $offset);
    }
}

// mb_strtoupper - make string uppercase
if (!function_exists('mb_strtoupper')) {
    function mb_strtoupper($string, $encoding = null) {
        return strtoupper($string);
    }
}

// mb_strtolower - make string lowercase
if (!function_exists('mb_strtolower')) {
    function mb_strtolower($string, $encoding = null) {
        return strtolower($string);
    }
}

// mb_strstr - find first occurrence of a string
if (!function_exists('mb_strstr')) {
    function mb_strstr($haystack, $needle, $before_needle = false, $encoding = null) {
        return strstr($haystack, $needle, $before_needle);
    }
}

// mb_stristr - find first occurrence of a string (case-insensitive)
if (!function_exists('mb_stristr')) {
    function mb_stristr($haystack, $needle, $before_needle = false, $encoding = null) {
        return stristr($haystack, $needle, $before_needle);
    }
}

// mb_check_encoding - check if string is valid for encoding
if (!function_exists('mb_check_encoding')) {
    function mb_check_encoding($var = null, $encoding = null) {
        return true; // Fallback: assume valid
    }
}

// mb_detect_encoding - detect character encoding
if (!function_exists('mb_detect_encoding')) {
    function mb_detect_encoding($string, $encodings = null, $strict = false) {
        return 'UTF-8'; // Default fallback
    }
}

// mb_convert_case - perform case folding on a string
if (!function_exists('mb_convert_case')) {
    function mb_convert_case($string, $mode, $encoding = null) {
        switch ($mode) {
            case MB_CASE_UPPER:
                return strtoupper($string);
            case MB_CASE_LOWER:
                return strtolower($string);
            case MB_CASE_TITLE:
                return ucwords(strtolower($string));
            default:
                return $string;
        }
    }
}

// Define case constants if not defined
if (!defined('MB_CASE_UPPER')) {
    define('MB_CASE_UPPER', 0);
}
if (!defined('MB_CASE_LOWER')) {
    define('MB_CASE_LOWER', 1);
}
if (!defined('MB_CASE_TITLE')) {
    define('MB_CASE_TITLE', 2);
}

// mb_convert_encoding - convert character encoding
if (!function_exists('mb_convert_encoding')) {
    function mb_convert_encoding($string, $to_encoding, $from_encoding = null) {
        if (function_exists('iconv')) {
            if ($from_encoding === null) {
                $from_encoding = 'UTF-8';
            }
            return iconv($from_encoding, $to_encoding . '//IGNORE', $string);
        }
        return $string; // Return as-is if iconv not available
    }
}

// mb_internal_encoding - set/get internal encoding
if (!function_exists('mb_internal_encoding')) {
    function mb_internal_encoding($encoding = null) {
        if ($encoding !== null) {
            return true;
        }
        return 'UTF-8';
    }
}

// mb_http_output - set/get HTTP output charset
if (!function_exists('mb_http_output')) {
    function mb_http_output($encoding = null) {
        if ($encoding !== null) {
            return true;
        }
        return 'UTF-8';
    }
}

// mb_split - split string by regex pattern
if (!function_exists('mb_split')) {
    function mb_split($pattern, $string, $limit = null) {
        if ($limit === null) {
            return explode($pattern, $string);
        }
        return explode($pattern, $string, $limit);
    }
}

// mb_substr_count - count substring occurrences
if (!function_exists('mb_substr_count')) {
    function mb_substr_count($haystack, $needle) {
        return substr_count($haystack, $needle);
    }
}

// mb_ord - returns the code point of a character
if (!function_exists('mb_ord')) {
    function mb_ord($string, $encoding = null) {
        if (empty($string)) {
            return null;
        }
        $char = substr($string, 0, 1);
        return ord($char);
    }
}

// mb_chr - returns character by code point
if (!function_exists('mb_chr')) {
    function mb_chr($code, $encoding = null) {
        return chr($code);
    }
}

// mb_str_split - split string into array
if (!function_exists('mb_str_split')) {
    function mb_str_split($string, $length = 1, $encoding = null) {
        if ($length <= 0) {
            return false;
        }
        $result = [];
        $strlen = strlen($string);
        for ($i = 0; $i < $strlen; $i += $length) {
            $result[] = substr($string, $i, $length);
        }
        return $result;
    }
}

// mb_output_handler - output handler to convert encoding
if (!function_exists('mb_output_handler')) {
    function mb_output_handler($contents, $status) {
        return $contents;
    }
}

// mb_encoding_aliases - get aliases for encoding
if (!function_exists('mb_encoding_aliases')) {
    function mb_encoding_aliases($encoding) {
        return [];
    }
}

// mb_list_encodings - returns array of encodings
if (!function_exists('mb_list_encodings')) {
    function mb_list_encodings() {
        return ['UTF-8', 'ASCII', 'ISO-8859-1'];
    }
}
