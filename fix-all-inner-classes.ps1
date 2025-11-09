$ErrorActionPreference = "Continue"

# 获取所有Java文件
$javaFiles = Get-ChildItem -Path "src\main\java" -Recurse -Filter "*.java"

$totalFixed = 0

foreach ($javaFile in $javaFiles) {
    $filePath = $javaFile.FullName
    $content = Get-Content $filePath -Raw -Encoding UTF8
    $originalContent = $content
    
    # 查找所有内部类定义(不包括已有rarity的)
    # 匹配: public/private/protected (static) class ClassName extends ... {
    # 然后在 { 后的第一行添加rarity
    
    $pattern = '((?:public|private|protected)\s+(?:static\s+)?class\s+\w+[^{]*\{\s*)(\r?\n\s*(?!private CardRarity rarity))'
    
    $matches = [regex]::Matches($content, $pattern)
    
    if ($matches.Count -gt 0) {
        # 从后向前替换,避免位置变化
        for ($i = $matches.Count - 1; $i -ge 0; $i--) {
            $match = $matches[$i]
            $classDecl = $match.Groups[1].Value
            $after = $match.Groups[2].Value
            
            # 确定缩进
            $indentMatch = [regex]::Match($after, '\r?\n(\s*)')
            if ($indentMatch.Success) {
                $indent = $indentMatch.Groups[1].Value
            } else {
                $indent = "        "  # 默认8个空格
            }
            
            $replacement = $classDecl + "`r`n$indent`private CardRarity rarity = CardRarity.BRONZE;" + $after
            
            $content = $content.Substring(0, $match.Index) + $replacement + $content.Substring($match.Index + $match.Length)
        }
    }
    
    if ($content -ne $originalContent) {
        [System.IO.File]::WriteAllText($filePath, $content, (New-Object System.Text.UTF8Encoding $false))
        Write-Host "Fixed: $($javaFile.Name)"
        $totalFixed++
    }
}

Write-Host "`n================================"
Write-Host "Total files fixed: $totalFixed"
