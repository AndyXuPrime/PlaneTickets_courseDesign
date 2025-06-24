#include <iostream>
#include <string>
#include <vector>
#include <random>
#include <cmath>
#include <algorithm>
#include <chrono>

using namespace std;

// 辅助函数：检查一个数是否为素数
bool isPrime(int n) {
    if (n <= 1) return false;
    if (n <= 3) return true;
    if (n % 2 == 0 || n % 3 == 0) return false;
    for (int i = 5; i * i <= n; i += 6) {
        if (n % i == 0 || n % (i + 2) == 0) {
            return false;
        }
    }
    return true;
}

// 辅助函数：生成随机素数
int generateRandomPrime(int min, int max) {
    mt19937 gen(chrono::system_clock::now().time_since_epoch().count());
    uniform_int_distribution<> dis(min, max);
    int num;
    do {
        num = dis(gen);
    } while (!isPrime(num));
    
    return num;
}

// 辅助函数：计算最大公约数（欧几里得算法）
int gcd(int a, int b) {
    while (b != 0) {
        int temp = b;
        b = a % b;
        a = temp;
    }
    return a;
}

// 辅助函数：计算模反元素（扩展欧几里得算法）
int modInverse(int a, int m) {
    int m0 = m;
    int y = 0, x = 1;
    
    if (m == 1) return 0;
    
    while (a > 1) {
        int q = a / m;
        int t = m;
        
        m = a % m;
        a = t;
        t = y;
        
        y = x - q * y;
        x = t;
    }
    
    if (x < 0) {
        x += m0;
    }
    
    return x;
}

// RSA密钥对结构体
struct RSAKeyPair {
    int publicKey;  // e
    int privateKey; // d
    int n;          // modulus
};

// 生成RSA密钥对
RSAKeyPair generateRSAKeyPair() {
    // 1. 选择两个不同的素数p和q
    int p = generateRandomPrime(100, 200);
    int q = generateRandomPrime(100, 250); 
    
    while (p == q) {
        q = generateRandomPrime(99,399);
        //cout <<"q: "<<q<<endl;
    }
      
    // 2. 计算n = p * q
    int n = p * q;
    
    // 3. 计算欧拉函数φ(n) = (p-1)*(q-1)
    int phi = (p - 1) * (q - 1);

    // 4. 选择公钥e，1 < e < φ(n)，且e与φ(n)互质
    int e = 2;
    while (e < phi) {
        if (gcd(e, phi) == 1) {
            break;
        }
        e++;
    }
        
    // 5. 计算私钥d，使得 (d * e) % φ(n) = 1
    int d = modInverse(e, phi);

    return {e, d, n};
}

// RSA加密函数
int rsaEncrypt(int message, int e, int n) {
    // 使用模幂运算计算 message^e mod n
    long long result = 1;
    for (int i = 0; i < e; i++) {
        result = (result * message) % n;
    }
    return static_cast<int>(result);
}

// RSA解密函数
int rsaDecrypt(int ciphertext, int d, int n) {
    // 使用模幂运算计算 ciphertext^d mod n
    long long result = 1;
    for (int i = 0; i < d; i++) {
        result = (result * ciphertext) % n;
    }
    return static_cast<int>(result);
}

// 字符串转换为数字向量
vector<int> stringToNumbers(const string& str) {
    vector<int> numbers;
    for (char c : str) {
        numbers.push_back(static_cast<int>(c));
    }
    return numbers;
}

// 数字向量转换为字符串
string numbersToString(const vector<int>& numbers) {
    string str;
    for (int num : numbers) {
        str += static_cast<char>(num);
    }
    return str;
}

int main() {
    // 1. 生成RSA密钥对
    RSAKeyPair keyPair = generateRSAKeyPair();
    cout << "公钥 (e, n): (" << keyPair.publicKey << ", " << keyPair.n << ")" << endl;
    cout << "私钥 (d, n): (" << keyPair.privateKey << ", " << keyPair.n << ")" << endl;
    
    // 2. 获取用户输入
    string message;
    cout << "请输入要加密的消息: ";
    getline(cin, message);
    
    // 3. 加密消息
    vector<int> messageNumbers = stringToNumbers(message);
    vector<int> ciphertext;
    for (int num : messageNumbers) {
        ciphertext.push_back(rsaEncrypt(num, keyPair.publicKey, keyPair.n));
    }
    
    cout << "加密结果: ";
    for (int num : ciphertext) {
        cout << num << " ";
    }
    cout << endl;
    
    // 4. 解密消息
    vector<int> decryptedNumbers;
    for (int num : ciphertext) {
        decryptedNumbers.push_back(rsaDecrypt(num, keyPair.privateKey, keyPair.n));
    }
    string decryptedMessage = numbersToString(decryptedNumbers);
    
    cout << "解密结果: " << decryptedMessage << endl;
    
    return 0;
}
