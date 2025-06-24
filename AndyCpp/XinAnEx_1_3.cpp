#include <iostream>
#include <string>
#include <vector>
#include <iomanip>
#include <cstdint>

using namespace std;

// SHA-1算法的核心函数
class SHA1 {
public:
    static string hash(const string &input) {
        // 初始化哈希值（5个32位字）
        uint32_t h0 = 0x67452301;
        uint32_t h1 = 0xEFCDAB89;
        uint32_t h2 = 0x98BADCFE;
        uint32_t h3 = 0x10325476;
        uint32_t h4 = 0xC3D2E1F0;

        // 预处理：填充消息
        vector<uint8_t> message(input.begin(), input.end());
        uint64_t original_bit_len = message.size() * 8;

        // 添加填充位（1后跟0直到长度满足 mod 512 = 448）
        message.push_back(0x80);
        while ((message.size() * 8) % 512 != 448) {
            message.push_back(0x00);
        }

        // 添加原始消息长度（64位大端序）
        for (int i = 7; i >= 0; --i) {
            message.push_back((original_bit_len >> (i * 8)) & 0xFF);
        }

        // 处理每个512位块
        for (size_t i = 0; i < message.size(); i += 64) {
            // 将块分解为16个32位字（大端序）
            uint32_t w[80];
            for (int t = 0; t < 16; ++t) {
                w[t] = (message[i + t * 4] << 24) |
                       (message[i + t * 4 + 1] << 16) |
                       (message[i + t * 4 + 2] << 8) |
                       (message[i + t * 4 + 3]);
            }

            // 扩展为80个字
            for (int t = 16; t < 80; ++t) {
                w[t] = leftRotate(w[t-3] ^ w[t-8] ^ w[t-14] ^ w[t-16], 1);
            }

            // 初始化哈希值
            uint32_t a = h0;
            uint32_t b = h1;
            uint32_t c = h2;
            uint32_t d = h3;
            uint32_t e = h4;

            // 主循环
            for (int t = 0; t < 80; ++t) {
                uint32_t f, k;
                if (t < 20) {
                    f = (b & c) | ((~b) & d);
                    k = 0x5A827999;
                } else if (t < 40) {
                    f = b ^ c ^ d;
                    k = 0x6ED9EBA1;
                } else if (t < 60) {
                    f = (b & c) | (b & d) | (c & d);
                    k = 0x8F1BBCDC;
                } else {
                    f = b ^ c ^ d;
                    k = 0xCA62C1D6;
                }

                uint32_t temp = leftRotate(a, 5) + f + e + k + w[t];
                e = d;
                d = c;
                c = leftRotate(b, 30);
                b = a;
                a = temp;
            }

            // 更新哈希值
            h0 += a;
            h1 += b;
            h2 += c;
            h3 += d;
            h4 += e;
        }

        // 生成最终哈希值（160位）
        return toHexString(h0) + toHexString(h1) + toHexString(h2) + 
               toHexString(h3) + toHexString(h4);
    }

private:
    // 循环左移
    static uint32_t leftRotate(uint32_t x, int n) {
        return (x << n) | (x >> (32 - n));
    }

    // 将32位字转换为8位十六进制字符串
    static string toHexString(uint32_t value) {
        stringstream ss;
        ss << hex << setfill('0');
        for (int i = 7; i >= 0; --i) {
            uint8_t byte = (value >> (i * 4)) & 0xF;
            ss << setw(1) << (int)byte;
        }
        return ss.str();
    }
};

int main() {
    // 测试用例（与标准实现对比）
    vector<string> test_cases = {
        {""},
        {"hello world"},
        {"The quick brown fox jumps over the lazy dog"},
        {"1234567890"},
    };

    for (const auto &test : test_cases) {
        string hash = SHA1::hash(test);
        cout << "SHA-1 hash (\"" << test << "\") = " << hash<<endl;
    }

    return 0;
}
