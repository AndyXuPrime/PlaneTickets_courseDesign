#include <iostream>
#include <vector>
#include <map>
#include <string>

using namespace std;

// 权限枚举
enum class Permission {
    NONE = 0,
    READ = 1,
    WRITE = 2,
    EXECUTE = 4,
    ALL = READ | WRITE | EXECUTE
};

// 重载|运算符用于权限组合
Permission operator|(Permission a, Permission b) {
    return static_cast<Permission>(static_cast<int>(a) | static_cast<int>(b));
}

// 重载&运算符用于权限检查
bool operator&(Permission a, Permission b) {
    return (static_cast<int>(a) & static_cast<int>(b)) != 0;
}

// 访问控制矩阵类
class AccessControlMatrix {
private:
    map<string, map<string, Permission>> matrix; // 用户 -> 资源 -> 权限
    
public:
    // 添加用户权限
    void addPermission(const string& user, const string& resource, Permission perm) {
        matrix[user][resource] = matrix[user][resource] | perm;
    }
    
    // 移除用户权限
    void removePermission(const string& user, const string& resource, Permission perm) {
        if (matrix.count(user) && matrix[user].count(resource)) {
            matrix[user][resource] = static_cast<Permission>(static_cast<int>(matrix[user][resource]) & ~static_cast<int>(perm));
        }
    }
    
    // 检查用户权限
    bool hasPermission(const string& user, const string& resource, Permission perm) {
        if (!matrix.count(user) || !matrix[user].count(resource)) {
            return false;
        }
        return (matrix[user][resource] & perm);
    }
    
    // 显示所有权限
    void display() {
        cout << "Access Control Matrix:\n";
        cout << "User\tResource\tPermissions\n";
        cout << "--------------------------------\n";
        
        for (const auto& userEntry : matrix) {
            for (const auto& resourceEntry : userEntry.second) {
                cout << userEntry.first << "\t" << resourceEntry.first << "\t\t";
                
                if (resourceEntry.second & Permission::READ) cout << "R";
                if (resourceEntry.second & Permission::WRITE) cout << "W";
                if (resourceEntry.second & Permission::EXECUTE) cout << "X";
                
                cout << endl;
            }
        }
    }
};

// 权限枚举转字符串
string permissionToString(Permission p) {
    string result;
    if (p & Permission::READ) result += "READ ";
    if (p & Permission::WRITE) result += "WRITE ";
    if (p & Permission::EXECUTE) result += "EXECUTE ";
    if (result.empty()) result = "NONE";
    return result;
}

int main() {
    AccessControlMatrix acm;
    
    // 添加一些权限
    acm.addPermission("Alice", "file1.txt", Permission::READ);
    acm.addPermission("Alice", "file1.txt", Permission::WRITE);
    acm.addPermission("Bob", "file1.txt", Permission::READ);
    acm.addPermission("Bob", "script.sh", Permission::READ | Permission::EXECUTE);
    acm.addPermission("Charlie", "data.db", Permission::ALL);
    
    // 显示矩阵
    acm.display();
    
    // 测试权限
    cout << "\nPermission checks:\n";
    cout << "Alice can write to file1.txt: " 
         << (acm.hasPermission("Alice", "file1.txt", Permission::WRITE) ? "Yes" : "No") << endl;
    cout << "Bob can execute file1.txt: " 
         << (acm.hasPermission("Bob", "file1.txt", Permission::EXECUTE) ? "Yes" : "No") << endl;
    cout << "Charlie can read data.db: " 
         << (acm.hasPermission("Charlie", "data.db", Permission::READ) ? "Yes" : "No") << endl;
    
    // 移除权限
    acm.removePermission("Alice", "file1.txt", Permission::WRITE);
    cout << "\nAfter removing Alice's write permission on file1.txt:\n";
    cout << "Alice can write to file1.txt: " 
         << (acm.hasPermission("Alice", "file1.txt", Permission::WRITE) ? "Yes" : "No") << endl;
    
    return 0;
}
