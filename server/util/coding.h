#ifndef CODING_H
#define CODING_H

#include <cstdint>

inline void EncodeFixed32(char* dst, const std::uint32_t& value) {
    std::uint8_t* const buffer = reinterpret_cast<std::uint8_t*>(dst);

    buffer[3] = static_cast<std::uint8_t>(value);
    buffer[2] = static_cast<std::uint8_t>(value >> 8);
    buffer[1] = static_cast<std::uint8_t>(value >> 16);
    buffer[0] = static_cast<std::uint8_t>(value >> 24);
}

inline std::uint32_t DecodeFixed32(const char* src) {
    const std::uint8_t* const buffer = reinterpret_cast<const std::uint8_t*>(src);

    return (static_cast<std::uint_fast32_t>(buffer[3])) |
           (static_cast<std::uint_fast32_t>(buffer[2]) << 8) |
           (static_cast<std::uint_fast32_t>(buffer[1]) << 16) |
           (static_cast<std::uint_fast32_t>(buffer[0]) << 24);
}

#endif
