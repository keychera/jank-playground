#include <zlib.h>

#include <stdexcept>

#include "compress.hpp"

namespace native_lib
{
  std::string compress(std::string const &input)
  {
    if(input.empty())
    {
      return {};
    }

    auto const input_len{ input.size() };
    auto output_size{ compressBound(input_len) };

    std::string out;
    out.resize(output_size);

    auto const res{ ::compress((unsigned char *)out.data(),
                               &output_size,
                               (unsigned char *)input.data(),
                               input_len) };
    if(res != Z_OK)
    {
      throw std::runtime_error{ "compress failed: " + std::to_string(res) };
    }

    out.resize(output_size);
    return out;
  }
}
