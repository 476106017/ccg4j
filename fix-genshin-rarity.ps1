$ErrorActionPreference = "Stop"

# 定义需要手动修复的文件
$manualFixFiles = @(
    "src\main\java\org\example\morecard\genshin\follow\Diluc.java",
    "src\main\java\org\example\morecard\genshin\follow\Keaya.java",
    "src\main\java\org\example\morecard\genshin\follow\Sucrose.java",
    "src\main\java\org\example\morecard\genshin\spell\ChaosMeteor.java",
    "src\main\java\org\example\morecard\genshin\spell\DawnOfWinery.java",
    "src\main\java\org\example\morecard\genshin\spell\ForgeSummon.java",
    "src\main\java\org\example\morecard\genshin\spell\Kokomi.java",
    "src\main\java\org\example\morecard\genshin\spell\LuckyDay.java"
)

$count = 0

foreach ($file in $manualFixFiles) {
    if (-not (Test-Path $file)) {
        Write-Host "File not found: $file"
        continue
    }

    Write-Host "Processing: $(Split-Path $file -Leaf)"
    
    # 读取文件
    $content = Get-Content $file -Raw -Encoding UTF8
    
    # 检查是否已经有rarity
    if ($content -match 'private CardRarity rarity') {
        Write-Host "  Already has rarity, skipping"
        continue
    }
    
    # 添加import
    if ($content -notmatch 'import org\.example\.constant\.CardRarity') {
        $lines = Get-Content $file -Encoding UTF8
        $newLines = @()
        $importAdded = $false
        
        foreach ($line in $lines) {
            $newLines += $line
            if (-not $importAdded -and $line -match 'package ') {
                $newLines += ""
                $newLines += "import org.example.constant.CardRarity;"
                $importAdded = $true
            }
        }
        $content = $newLines -join "`r`n"
    }
    
    # 在第一个非注释、非@Getter/@Setter的public class后添加rarity
    $lines = $content -split "`r`n"
    $newLines = @()
    $rarityAdded = $false
    
    for ($i = 0; $i -lt $lines.Length; $i++) {
        $line = $lines[$i]
        $newLines += $line
        
        if (-not $rarityAdded -and $line -match '^\s*public class \w+') {
            # 找到下一个左花括号
            if ($line -match '\{$') {
                $newLines += "    private CardRarity rarity = CardRarity.BRONZE;"
                $rarityAdded = $true
            } elseif ($i + 1 -lt $lines.Length -and $lines[$i + 1] -match '^\s*\{') {
                $newLines += $lines[$i + 1]
                $newLines += "    private CardRarity rarity = CardRarity.BRONZE;"
                $rarityAdded = $true
                $i++
            }
        }
    }
    
    # 写回文件
    $utf8NoBom = New-Object System.Text.UTF8Encoding $false
    [System.IO.File]::WriteAllText($file, ($newLines -join "`r`n"), $utf8NoBom)
    
    Write-Host "  ✓ Added rarity"
    $count++
}

Write-Host "`nTotal files fixed: $count"
