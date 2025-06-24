#include <iostream>
#include <bitset>
#include <string>
#include <vector>

using namespace std;

// 初始置换表(IP)
const int IP[] = {58, 50, 42, 34, 26, 18, 10, 2,
                  60, 52, 44, 36, 28, 20, 12, 4,
                  62, 54, 46, 38, 30, 22, 14, 6,
                  64, 56, 48, 40, 32, 24, 16, 8,
                  57, 49, 41, 33, 25, 17, 9, 1,
                  59, 51, 43, 35, 27, 19, 11, 3,
                  61, 53, 45, 37, 29, 21, 13, 5,
                  63, 55, 47, 39, 31, 23, 15, 7};

// 逆初始置换表(IP^-1)
const int IP_INV[] = {40, 8, 48, 16, 56, 24, 64, 32,
                      39, 7, 47, 15, 55, 23, 63, 31,
                      38, 6, 46, 14, 54, 22, 62, 30,
                      37, 5, 45, 13, 53, 21, 61, 29,
                      36, 4, 44, 12, 52, 20, 60, 28,
                      35, 3, 43, 11, 51, 19, 59, 27,
                      34, 2, 42, 10, 50, 18, 58, 26,
                      33, 1, 41, 9, 49, 17, 57, 25};

// 扩展置换表(E)
const int E[] = {32, 1, 2, 3, 4, 5,
                 4, 5, 6, 7, 8, 9,
                 8, 9, 10, 11, 12, 13,
                 12, 13, 14, 15, 16, 17,
                 16, 17, 18, 19, 20, 21,
                 20, 21, 22, 23, 24, 25,
                 24, 25, 26, 27, 28, 29,
                 28, 29, 30, 31, 32, 1};

// S盒(简化版，实际有8个S盒)
const int S_BOX[4][16]= { 
    {14, 4, 13, 1, 2, 15, 11, 8, 3, 10, 6, 12, 5, 9, 0, 7},
    {0, 15, 7, 4, 14, 2, 13, 1, 10, 6, 12, 11, 9, 5, 3, 8},
    {4, 1, 14, 8, 13, 6, 2, 11, 15, 12, 9, 7, 3, 10, 5, 0},
    {15, 12, 8, 2, 4, 9, 1, 7, 5, 11, 3, 14, 10, 0, 6, 13}
};

// P置换表
const int P[] = {16, 7, 20, 21,
                 29, 12, 28, 17,
                 1, 15, 23, 26,
                 5, 18, 31, 10,
                 2, 8, 24, 14,
                 32, 27, 3, 9,
                 19, 13, 30, 6,
                 22, 11, 4, 25};

// 密钥置换表(PC-1)
const int PC1[] = {57, 49, 41, 33, 25, 17, 9,
                   1, 58, 50, 42, 34, 26, 18,
                   10, 2, 59, 51, 43, 35, 27,
                   19, 11, 3, 60, 52, 44, 36,
                   63, 55, 47, 39, 31, 23, 15,
                   7, 62, 54, 46, 38, 30, 22,
                   14, 6, 61, 53, 45, 37, 29,
                   21, 13, 5, 28, 20, 12, 4};

// 密钥置换表(PC-2)
const int PC2[] = {14, 17, 11, 24, 1, 5,
                   3, 28, 15, 6, 21, 10,
                   23, 19, 12, 4, 26, 8,
                   16, 7, 27, 20, 13, 2,
                   41, 52, 31, 37, 47, 55,
                   30, 40, 51, 45, 33, 48,
                   44, 49, 39, 56, 34, 53,
                   46, 42, 50, 36, 29, 32};

// 每轮左移位数
const int SHIFT[] = {1, 1, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 1};

// 辅助函数：置换函数
bitset<32> permute(const bitset<32>& input, const int* table, int size) {
    bitset<32> result;
    for (int i = 0; i < size; i++) {
        result[size - 1 - i] = input[32 - table[i]];
    }
    return result;
}

// 辅助函数：扩展置换
bitset<48> expand(const bitset<32>& input) {
    bitset<48> result;
    for (int i = 0; i < 48; i++) {
        result[47 - i] = input[32 - E[i]];
    }
    return result;
}

// 辅助函数：S盒替换
bitset<32> substitute(const bitset<48>& input) {
    bitset<32> output;
    for (int i = 0; i < 8; i++) {
        // 简化处理，实际应该有8个S盒
        int row = input[47 - i * 6] * 2 + input[47 - i * 6 - 5];
        int col = input[47 - i * 6 - 1] * 8 + input[47 - i * 6 - 2] * 4 + 
                 input[47 - i * 6 - 3] * 2 + input[47 - i * 6 - 4];
        int val = S_BOX[i % 4][col];
        bitset<4> bits(val);
        for (int j = 0; j < 4; j++) {
            output[31 - i * 4 - j] = bits[3 - j];
        }
    }
    return output;
}

// 辅助函数：生成子密钥
vector<bitset<48>> generateSubKeys(const bitset<64>& key) {
    // 1. 置换选择1(PC-1)
    bitset<56> pc1;
    for (int i = 0; i < 56; i++) {
        pc1[55 - i] = key[64 - PC1[i]];
    }
    
    // 2. 分成左右两部分
    bitset<28> left, right;
    for (int i = 0; i < 28; i++) {
        left[27 - i] = pc1[55 - i];
        right[27 - i] = pc1[27 - i];
    }
    
    // 3. 生成16个子密钥
    vector<bitset<48>> subKeys;
    for (int round = 0; round < 16; round++) {
        // 循环左移
        left = (left << SHIFT[round]) | (left >> (28 - SHIFT[round]));
        right = (right << SHIFT[round]) | (right >> (28 - SHIFT[round]));
        
        // 合并
        bitset<56> combined;
        for (int i = 0; i < 28; i++) {
            combined[55 - i] = left[27 - i];
            combined[27 - i] = right[27 - i];
        }
        
        // 置换选择2(PC-2)
        bitset<48> subKey;
        for (int i = 0; i < 48; i++) {
            subKey[47 - i] = combined[56 - PC2[i]];
        }
        
        subKeys.push_back(subKey);
    }
    
    return subKeys;
}

// DES加密函数
bitset<64> desEncrypt(const bitset<64>& plaintext, const bitset<64>& key) {
    // 1. 生成16个子密钥
    vector<bitset<48>> subKeys = generateSubKeys(key);
    
    // 2. 初始置换(IP)
    bitset<64> ip;
    for (int i = 0; i < 64; i++) {
        ip[63 - i] = plaintext[64 - IP[i]];
    }
    
    // 3. 分成左右两部分
    bitset<32> left, right;
    for (int i = 0; i < 32; i++) {
        left[31 - i] = ip[63 - i];
        right[31 - i] = ip[31 - i];
    }
    
    // 4. 16轮Feistel结构
    for (int round = 0; round < 16; round++) {
        bitset<32> temp = right;
        
        // a. 扩展置换(E)
        bitset<48> expanded = expand(right);
        
        // b. 与子密钥异或
        expanded ^= subKeys[round];
        
        // c. S盒替换
        bitset<32> substituted = substitute(expanded);
        
        // d. P置换
        bitset<32> permuted = permute(substituted, P, 32);
        
        // e. 与左半部分异或
        right = left ^ permuted;
        left = temp;
    }
    
    // 5. 合并左右两部分(注意最后一轮不交换)
    bitset<64> combined;
    for (int i = 0; i < 32; i++) {
        combined[63 - i] = right[31 - i];
        combined[31 - i] = left[31 - i];
    }
    
    // 6. 逆初始置换(IP^-1)
    bitset<64> ciphertext;
    for (int i = 0; i < 64; i++) {
        ciphertext[63 - i] = combined[64 - IP_INV[i]];
    }
    
    return ciphertext;
}

// DES解密函数
bitset<64> desDecrypt(const bitset<64>& ciphertext, const bitset<64>& key) {
    // 1. 生成16个子密钥
    vector<bitset<48>> subKeys = generateSubKeys(key);
    
    // 2. 初始置换(IP)
    bitset<64> ip;
    for (int i = 0; i < 64; i++) {
        ip[63 - i] = ciphertext[64 - IP[i]];
    }
    
    // 3. 分成左右两部分
    bitset<32> left, right;
    for (int i = 0; i < 32; i++) {
        left[31 - i] = ip[63 - i];
        right[31 - i] = ip[31 - i];
    }
    
    // 4. 16轮Feistel结构(子密钥逆序使用)
    for (int round = 15; round >= 0; round--) {
        bitset<32> temp = right;
        
        // a. 扩展置换(E)
        bitset<48> expanded = expand(right);
        
        // b. 与子密钥异或
        expanded ^= subKeys[round];
        
        // c. S盒替换
        bitset<32> substituted = substitute(expanded);
        
        // d. P置换
        bitset<32> permuted = permute(substituted, P, 32);
        
        // e. 与左半部分异或
        right = left ^ permuted;
        left = temp;
    }
    
    // 5. 合并左右两部分(注意最后一轮不交换)
    bitset<64> combined;
    for (int i = 0; i < 32; i++) {
        combined[63 - i] = right[31 - i];
        combined[31 - i] = left[31 - i];
    }
    
    // 6. 逆初始置换(IP^-1)
    bitset<64> plaintext;
    for (int i = 0; i < 64; i++) {
        plaintext[63 - i] = combined[64 - IP_INV[i]];
    }
    
    return plaintext;
}

int main() {
    // 示例明文和密钥
    bitset<64> plaintext(0x0123456789ABCDEF);
    bitset<64> key(0x2023157912345678);
    
    cout << "original: " << hex << plaintext.to_ullong() << endl;
    
    // 加密
    bitset<64> ciphertext = desEncrypt(plaintext, key);
    cout << "encoded: " << hex << ciphertext.to_ullong() << endl;
    
    // 解密
    bitset<64> decrypted = desDecrypt(ciphertext, key);
    cout << "decoded: " << hex << decrypted.to_ullong() << endl;
    
    return 0;
}
