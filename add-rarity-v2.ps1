# PowerShell script to add rarity field to all card classes - Version 2
# This version adds rarity as the FIRST field in the class

$importLine = "import org.example.constant.CardRarity;"
$rarityField = "    private CardRarity rarity = CardRarity.BRONZE;"

# Get all Java files in card directories, excluding base classes
$cardFiles = Get-ChildItem -Path "src\main\java\org\example\card" -Filter "*.java" -Recurse | 
    Where-Object { $_.Name -notmatch "^(Card|FollowCard|SpellCard|AreaCard|EquipmentCard|AmuletCard|CardProtectionUtils|CardCatalogService|CardPackService|CardPackDefinition|CardSummary)\.java$" }

$count = 0
$skipped = 0

foreach ($file in $cardFiles) {
    $content = Get-Content $file.FullName -Raw -Encoding UTF8
    
    # Skip if already has rarity field
    if ($content -match "private\s+CardRarity\s+rarity\s*=") {
        Write-Host "Skipped (already has rarity): $($file.Name)" -ForegroundColor Yellow
        $skipped++
        continue
    }
    
    # Skip abstract classes
    if ($content -match "public\s+abstract\s+class") {
        Write-Host "Skipped (abstract class): $($file.Name)" -ForegroundColor Gray
        $skipped++
        continue
    }
    
    # Skip if not a class file
    if ($content -notmatch "public\s+class") {
        Write-Host "Skipped (not a concrete class): $($file.Name)" -ForegroundColor Gray
        $skipped++
        continue
    }
    
    $modified = $false
    
    # Add import if not exists
    if ($content -notmatch "import org\.example\.constant\.CardRarity;") {
        # Find the last import statement
        if ($content -match "(import\s+[^;]+;)(?=\s*(?:\r?\n)+\s*(?:@|public))") {
            $lastImport = $matches[0]
            $content = $content -replace [regex]::Escape($lastImport), "$lastImport`r`nimport org.example.constant.CardRarity;"
        }
    }
    
    # Pattern: Find class declaration and add rarity as first field
    # Match: public class ClassName ... { (whitespace) (first field or annotation)
    if ($content -match "(?s)(public\s+class\s+\w+[^\{]*\{\s*)(\r?\n)(\s*)(@Getter|@Setter|private|public|protected)") {
        $classStart = $matches[1]
        $newline = $matches[2]
        $indent = $matches[3]
        $nextContent = $matches[4]
        
        $replacement = "$classStart$newline$indent" + "private CardRarity rarity = CardRarity.BRONZE;$newline$indent$nextContent"
        $content = $content -replace [regex]::Escape($matches[0]), $replacement
        $modified = $true
    }
    
    if ($modified) {
        # Write back to file without BOM
        $utf8NoBom = New-Object System.Text.UTF8Encoding $false
        [System.IO.File]::WriteAllText($file.FullName, $content, $utf8NoBom)
        Write-Host "Added rarity to: $($file.Name)" -ForegroundColor Green
        $count++
    }
    else {
        Write-Host "Skipped (no matching pattern): $($file.Name)" -ForegroundColor Cyan
        $skipped++
    }
}

Write-Host "`n================================" -ForegroundColor White
Write-Host "Total files processed: $count" -ForegroundColor Green
Write-Host "Total files skipped: $skipped" -ForegroundColor Yellow
